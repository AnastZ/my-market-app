package ru.ya.practicum.mymarket.controllers;


import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.services.OrderService;

@Controller
@RequestMapping("/buy")
public class BuyController {

    private final OrderService orderService;

    public BuyController(@NotNull final OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Оформление (создание) заказа и перенаправление на другую страницу.
     *
     * @param user данные о пользователе.
     * @return источник представления.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> buy(@AuthenticationPrincipal @NotNull final UserDetails user) {
        return orderService.createOrder(user.getUsername())
                .map(order -> Rendering.redirectTo("orders/{id}")
                        .modelAttribute("id", order.id())
                        .modelAttribute("newOrder", true)
                        .build());
    }

}
