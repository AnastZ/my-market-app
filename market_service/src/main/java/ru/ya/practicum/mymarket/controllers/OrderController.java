package ru.ya.practicum.mymarket.controllers;

import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.services.OrderService;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(@NotNull final OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Получить все заказы.
     *
     * @return представление "orders" с атрибутом модели "orders".
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> getOrders(@AuthenticationPrincipal @NotNull final UserDetails user) {
        return Mono.just(Rendering.view("orders")
                .modelAttribute("orders", orderService.findAll(user.getUsername()))
                .build());
    }

    /**
     * Получить заказ по его уникальному номеру.
     *
     * @param orderId  уникальный номер заказа.
     * @param newOrder является ли заказ новым.
     * @return представление "order" с атрибутомами модели: "order", "newOrder".
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated() and @orderService.isOrderOwner(#user.username, #orderId)")
    public Mono<Rendering> getOrder(@AuthenticationPrincipal @NotNull final UserDetails user,
                                    @PathVariable("id") final Long orderId,
                                    @RequestParam(name = "newOrder", required = false, defaultValue = "false") final boolean newOrder) {

        return Mono.just(Rendering.view("order")
                .modelAttribute("order", orderService.findById(orderId, user.getUsername()))
                .modelAttribute("newOrder", newOrder)
                .build());
    }
}
