package ru.yandex.practicum.mymarket.controllers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.mymarket.controllers.dto.CartDTO;
import ru.yandex.practicum.mymarket.services.CartService;

import java.util.Objects;

@Controller
@RequestMapping("/cart/items")
public class CartController {

    private final CartService cartService;

    public CartController(@NotNull final CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String getCart(@NotNull final Model model,
                          @NotNull final HttpSession session) {
        final CartDTO cartDTO = cartService.getCartBySessionId(session.getId());
        model.addAttribute("items", cartDTO.items());
        model.addAttribute("total", cartDTO.total());
        return "cart";
    }

    @PostMapping
    public String changeItem(@RequestParam("id") final Long itemId,
                             @RequestParam(value = "action") final ItemController.CartItemAction action,
                             @NotNull final Model model,
                             @NotNull final HttpSession session) {

        if (Objects.nonNull(action)) {
            switch (action) {
                case PLUS -> cartService.incrementItem(itemId, session.getId());
                case MINUS -> cartService.decrementItem(itemId, session.getId());
            }
        }
        return getCart(model, session);
    }

}
