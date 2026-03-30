package ru.ya.practicum.mymarket.controllers;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.services.CartService;
import ru.ya.practicum.mymarket.services.ItemService;

@Controller
@RequestMapping("/cart/items")
public class CartController {

    private final CartService cartService;
    private final ItemService  itemService;

    public CartController(@NotNull final CartService cartService,
                          @NotNull final ItemService itemService) {
        this.cartService = cartService;
        this.itemService = itemService;
    }

    @GetMapping
    public Mono<Rendering> getCart(@NotNull final WebSession session) {
        return cartService.getCartBySessionId(session.getId())
                .map(cart -> Rendering.view("cart")
                        .modelAttribute("items", cart.items())
                        .modelAttribute("total", cart.total())
                        .modelAttribute("successBuy",  cart.successBuy())
                        .build());
    }

    @PostMapping
    public Mono<Rendering> changeItem(@RequestParam("id") final Long itemId,
                                      @RequestParam(value = "action") final ItemController.CartItemAction action,
                                      @NotNull final WebSession session) {

        final Mono<Void> cartItemAction = switch (action) {
            case PLUS -> itemService.incrementItem(itemId, session.getId());
            case MINUS -> itemService.decrementItem(itemId, session.getId());
            case null, default -> Mono.empty();
        };
        return cartItemAction.then(Mono.defer(() -> getCart(session)));
    }

}
