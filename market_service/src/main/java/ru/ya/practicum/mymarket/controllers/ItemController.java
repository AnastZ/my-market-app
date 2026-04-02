package ru.ya.practicum.mymarket.controllers;

import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.model.SortMethod;
import ru.ya.practicum.mymarket.services.ItemService;

import java.util.Objects;

@Controller
@RequestMapping(path = { "/items"})
public class ItemController {

    private ItemService itemService;

    public ItemController(@NotNull final ItemService itemService) {
        this.itemService = itemService;
    }


    /**
     * Получить объекты на заданной странице.
     *
     * @param user       данные об аутентификации пользователя.
     * @param search     поисковой запрос (фильтрация по названию или описанию).
     * @param sort       метод сортировки.
     * @param pageNumber номер страницы.
     * @param pageSize   количество объектов на странице.
     * @return html Thymeleaf шаблон.
     */
    @GetMapping
    public Mono<Rendering> getItems(@AuthenticationPrincipal final UserDetails user,
                                    @RequestParam(value = "search", required = false, defaultValue = "") final String search,
                                    @RequestParam(value = "sort", required = false, defaultValue = "NO") @NotNull final SortMethod sort,
                                    @RequestParam(value = "pageNumber", required = false, defaultValue = "1") final int pageNumber,
                                    @RequestParam(value = "pageSize", required = false, defaultValue = "5") final int pageSize) {
        return itemService.getItems(pageNumber, pageSize, search, sort, Objects.isNull(user) ? "anonymous" : user.getUsername())
                .map(dto -> Rendering.view("items")
                        .modelAttribute("items", dto.items())
                        .modelAttribute("search", Objects.isNull(search) ? "" : search)
                        .modelAttribute("sort", sort)
                        .modelAttribute("paging", dto.paging())
                        .build());
    }

    public enum CartItemAction {
        PLUS, MINUS
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> changeItemInCart(@AuthenticationPrincipal final UserDetails user,
                                            @RequestParam("id") final Long id,
                                            @RequestParam(value = "search", required = false, defaultValue = "") final String search,
                                            @RequestParam(value = "sort", required = false, defaultValue = "NO") @NotNull final SortMethod sort,
                                            @RequestParam(value = "pageNumber", required = false, defaultValue = "1") final int pageNumber,
                                            @RequestParam(value = "pageSize", required = false, defaultValue = "5") final int pageSize,
                                            @RequestParam(value = "action") final CartItemAction action) {

        return (switch (action) {
            case PLUS -> itemService.incrementItem(id, user.getUsername());
            case MINUS -> itemService.decrementItem(id, user.getUsername());
        })
                .then(Mono.fromCallable(() -> Rendering.redirectTo("items")
                        .modelAttribute("search", search)
                        .modelAttribute("sort", sort)
                        .modelAttribute("pageNumber", pageNumber)
                        .modelAttribute("pageSize", pageSize)
                        .build()));


    }

    @GetMapping("/{id}")
    public Mono<Rendering> getItem(@AuthenticationPrincipal final UserDetails user,
                                   @PathVariable("id") final Long itemId) {

        return itemService.findItemInCart(itemId, getUsername(user))
                .map(dto -> Rendering.view("item")
                        .modelAttribute("item", dto)
                        .build());
    }

    @PostMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> changeItemInCart(@AuthenticationPrincipal final UserDetails user,
                                            @PathVariable("id") final Long itemId,
                                            @RequestParam(value = "action") final CartItemAction action) {

        return (switch (action) {
            case PLUS -> itemService.incrementItem(itemId, getUsername(user));
            case MINUS -> itemService.decrementItem(itemId, getUsername(user));
            case null, default -> Mono.empty();
        }).then(itemService.findItemInCart(itemId, getUsername(user))
                .map(it -> Rendering.view("item")
                        .modelAttribute("item", it)
                        .build()));
    }
    private String getUsername(final UserDetails user) {
        return Objects.isNull(user) ? "anonymous" : user.getUsername();
    }
}
