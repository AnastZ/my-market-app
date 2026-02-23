package ru.yandex.practicum.mymarket.controllers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
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
    public String getItems(@NotNull final Model model,
                           @NotNull final HttpSession session,
                           @RequestParam(value = "search", required = false, defaultValue = "") final String search,
                           @RequestParam(value = "sort", required = false, defaultValue = "NO") @NotNull final SortMethod sort,
                           @RequestParam(value = "pageNumber", required = false, defaultValue = "1") final int pageNumber,
                           @RequestParam(value = "pageSize", required = false, defaultValue = "5") final int pageSize) {

        final ItemsDTO dto = itemService.getItems(pageNumber, pageSize, search, sort, session.getId());
        model.addAttribute("items", dto.items());
        model.addAttribute("search", Objects.isNull(search) ? "" : search);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", dto.paging());
        return "items";
    }

    public enum CartItemAction {
        PLUS, MINUS
    }

    @PostMapping
    public RedirectView changeItemInCart(@NotNull final HttpSession session,
                                         @NotNull final RedirectAttributes redirectAttributes,
                                         @RequestParam("id") final Long id,
                                         @RequestParam(value = "search", required = false, defaultValue = "") final String search,
                                         @RequestParam(value = "sort", required = false, defaultValue = "NO") @NotNull final SortMethod sort,
                                         @RequestParam(value = "pageNumber", required = false, defaultValue = "1") final int pageNumber,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "5") final int pageSize,
                                         @RequestParam(value = "action") final CartItemAction action) throws MissingServletRequestParameterException {
        switch (action) {
            case PLUS -> itemService.incrementItem(id, session.getId());
            case MINUS -> itemService.decrementItem(id, session.getId());
        }
        redirectAttributes.addAttribute("search", search);
        redirectAttributes.addAttribute("sort", sort);
        redirectAttributes.addAttribute("pageNumber", pageNumber);
        redirectAttributes.addAttribute("pageSize", pageSize);
        final RedirectView view = new RedirectView("items");
        return view;
    }

    @GetMapping("/{id}")
    public String getItem(@PathVariable("id") final Long itemId,
                          @NotNull final Model model,
                          @NotNull final HttpSession session) {

        model.addAttribute("item", itemService.findItem(itemId, session.getId()));
        return "item";
    }

    @PostMapping("/{id}")
    public String changeItemInCart(@PathVariable("id") final Long itemId,
                                   @RequestParam(value = "action") final CartItemAction action,
                                   @NotNull final Model model,
                                   @NotNull final HttpSession session) {

        if (Objects.nonNull(action)) {
            switch (action) {
                case PLUS -> itemService.incrementItem(itemId, session.getId());
                case MINUS -> itemService.decrementItem(itemId, session.getId());
            }
        }
        model.addAttribute("item", itemService.findItem(itemId, session.getId()));
        return "item";
    }
}
