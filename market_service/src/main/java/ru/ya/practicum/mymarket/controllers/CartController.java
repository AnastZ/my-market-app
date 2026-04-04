package ru.ya.practicum.mymarket.controllers;

import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.services.CartService;
import ru.ya.practicum.mymarket.services.ItemService;

@Controller
@RequestMapping("/cart/items")
public class CartController {

    private final CartService cartService;
    private final ItemService itemService;

    public CartController(@NotNull final CartService cartService,
                          @NotNull final ItemService itemService) {
        this.cartService = cartService;
        this.itemService = itemService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> getCart(@AuthenticationPrincipal @NotNull final UserDetails user) {
        return cartService.getCart(user.getUsername())
                .map(cart -> Rendering.view("cart")
                        .modelAttribute("items", cart.items())
                        .modelAttribute("total", cart.total())
                        .modelAttribute("successBuy", cart.successBuy())
                        .build());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> changeItem(@AuthenticationPrincipal @NotNull final UserDetails user,
                                      @RequestParam("id") final Long itemId,
                                      @RequestParam(value = "action") final ItemController.CartItemAction action) {

        final Mono<Void> cartItemAction = switch (action) {
            case PLUS -> itemService.incrementItem(itemId, user.getUsername());
            case MINUS -> itemService.decrementItem(itemId, user.getUsername());
            case null -> Mono.empty();
        };
        return cartItemAction.then(Mono.defer(() -> getCart(user)));
    }

}
