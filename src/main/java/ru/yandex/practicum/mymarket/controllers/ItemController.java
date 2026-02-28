package ru.yandex.practicum.mymarket.controllers;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.controllers.dto.ItemsDTO;
import ru.yandex.practicum.mymarket.services.ItemService;

import java.util.Objects;

@Controller
@RequestMapping(path = {"/", "/items"})
public class ItemController {

    private ItemService itemService;

    public ItemController(@NotNull final ItemService itemService) {
        this.itemService = itemService;
    }

    public enum SortMethod {
        NO, ALPHA, PRICE;
    }

    /**
     * Получить объекты на заданной странице.
     *
     * @param model      модель данных.
     * @param session    сессия.
     * @param search     поисковой запрос (фильтрация по названию или описанию).
     * @param sort       метод сортировки.
     * @param pageNumber номер страницы.
     * @param pageSize   количество объектов на странице.
     * @return html Thymeleaf шаблон.
     */
    @GetMapping
    public Mono<Rendering> getItems(@NotNull final Model model,
                                    @NotNull final WebSession session,
                                    @RequestParam(value = "search", required = false, defaultValue = "") final String search,
                                    @RequestParam(value = "sort", required = false, defaultValue = "NO") @NotNull final SortMethod sort,
                                    @RequestParam(value = "pageNumber", required = false, defaultValue = "1") final int pageNumber,
                                    @RequestParam(value = "pageSize", required = false, defaultValue = "5") final int pageSize) {

        return itemService.getItems(pageNumber, pageSize, search, sort, session.getId())
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
    public Mono<Rendering> changeItemInCart(@NotNull final WebSession session,
                                            @RequestParam("id") final Long id,
                                            @RequestParam(value = "search", required = false, defaultValue = "") final String search,
                                            @RequestParam(value = "sort", required = false, defaultValue = "NO") @NotNull final SortMethod sort,
                                            @RequestParam(value = "pageNumber", required = false, defaultValue = "1") final int pageNumber,
                                            @RequestParam(value = "pageSize", required = false, defaultValue = "5") final int pageSize,
                                            @RequestParam(value = "action") final CartItemAction action) {

        return (switch (action) {
            case PLUS -> itemService.incrementItem(id, session.getId());
            case MINUS -> itemService.decrementItem(id, session.getId());
        })
                .then(Mono.fromCallable(() -> Rendering.redirectTo("items")
                        .modelAttribute("search", search)
                        .modelAttribute("sort", sort)
                        .modelAttribute("pageNumber", pageNumber)
                        .modelAttribute("pageSize", pageSize)
                        .build()));


    }

    @GetMapping("/{id}")
    public Mono<Rendering> getItem(@PathVariable("id") final Long itemId,
                                   @NotNull final WebSession session) {

        return itemService.findItemInCart(itemId, session.getId())
                .map(dto -> Rendering.view("item")
                        .modelAttribute("item", dto)
                        .build());
    }

    @PostMapping("/{id}")
    public Mono<Rendering> changeItemInCart(@PathVariable("id") final Long itemId,
                                            @RequestParam(value = "action") final CartItemAction action,
                                            @NotNull final WebSession session) {

        return (switch (action) {
            case PLUS -> itemService.incrementItem(itemId, session.getId());
            case MINUS -> itemService.decrementItem(itemId, session.getId());
            case null, default -> Mono.empty();
        }).then(itemService.findItemInCart(itemId, session.getId())
                .map(it -> Rendering.view("item")
                        .modelAttribute("item", it)
                        .build()));
    }
}
