package ru.yandex.practicum.mymarket.controllers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import ru.yandex.practicum.mymarket.controllers.dto.OrderDTO;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.services.OrderService;

@Controller
@RequestMapping("/buy")
public class BuyController {

    private final OrderService orderService;

    public BuyController(@NotNull final OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public RedirectView buy(@NotNull final RedirectAttributes attributes,
                            @NotNull final HttpSession session) {
        final OrderDTO order = orderService.save(session.getId());
        final RedirectView view = new RedirectView("orders/{id}");
        attributes.addAttribute("id", order.id());
        attributes.addAttribute("newOrder", true);
        return view;
    }

}
