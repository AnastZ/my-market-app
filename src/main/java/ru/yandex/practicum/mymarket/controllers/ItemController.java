package ru.yandex.practicum.mymarket.controllers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    public enum SortMethod{
        NO, ALPHA, PRICE;
    }

    /**
     * Получить объекты на заданной странице.
     * @param model модель данных.
     * @param session сессия.
     * @param search поисковой запрос (фильтрация по названию или описанию).
     * @param sort метод сортировки.
     * @param pageNumber номер страницы.
     * @param pageSize количество объектов на странице.
     * @return html Thymeleaf шаблон.
     */
    @GetMapping
    public String getItems(@NotNull final Model model,
                           @NotNull final HttpSession session,
                           @RequestParam(value = "search", required = false) final String search,
                           @RequestParam(value = "sort", required = false, defaultValue = "NO") @NotNull final SortMethod sort,
                           @RequestParam("pageNumber") final int pageNumber,
                           @RequestParam("pageSize") final int pageSize){

        final ItemsDTO dto = itemService.getItems(pageNumber, pageSize, search, sort, session.getId());
        model.addAttribute("items", dto.items());
        model.addAttribute("search", Objects.isNull(search) ? "" : search);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", dto.paging());
        return "items";
    }


}
