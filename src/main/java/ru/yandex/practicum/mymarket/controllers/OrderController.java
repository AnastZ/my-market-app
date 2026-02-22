package ru.yandex.practicum.mymarket.controllers;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.controllers.dto.OrderDTO;
import ru.yandex.practicum.mymarket.services.OrderService;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService  orderService;

    public OrderController(@NotNull final OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String orders(@NotNull final Model model) {
        final List<OrderDTO> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/{id}")
    public String newOrder(@NotNull final Model model,
                           @PathVariable("id") final Long orderId,
                           @RequestParam(name = "newOrder", required = false, defaultValue = "false") final boolean newOrder)  throws MissingServletRequestParameterException {
        System.err.println("redirect true");
        final OrderDTO order = orderService.findById(orderId);
        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);
        return "order";
    }
}
