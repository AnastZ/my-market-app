package ru.yandex.practicum.mymarket.controllers;


import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.services.OrderService;

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
     * @param session сессия.
     * @return источник представления.
     */
    @PostMapping
    public Mono<Rendering> buy(@NotNull final WebSession session) {

        return orderService.save(session.getId())
                .map(order -> Rendering.redirectTo("orders/{id}")
                        .modelAttribute("id", order.id())
                        .modelAttribute("newOrder", true)
                        .build());
    }

}
