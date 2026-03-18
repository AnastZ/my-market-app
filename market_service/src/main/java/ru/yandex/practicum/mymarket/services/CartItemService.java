package ru.yandex.practicum.mymarket.services;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;

@Service
public class CartItemService {

    private final Logger log = LoggerFactory.getLogger(CartItemService.class);

    private final CartItemRepository cartItemRepository;

    public CartItemService(@NotNull final CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional(readOnly = true)
    public Mono<CartItem> findByIdInCart(@NotNull final Long itemId,
                                         @NotNull @NotBlank final String sessionId) {
        return cartItemRepository.findByItemIdAndSessionId(itemId, sessionId);
    }

    @Transactional(readOnly = true)
    public Flux<CartItem> findItemsBySessionId(@NotNull @NotBlank final String sessionId) {
        return cartItemRepository.getCartItems(sessionId);
    }

    @Transactional
    public Mono<CartItem> save(@NotNull final CartItem cartItem) {
        return cartItemRepository.save(cartItem)
                .switchIfEmpty(Mono.error(new IllegalArgumentException()));
    }

    @Transactional
    public Mono<Void> delete(@NotNull final CartItem cartItem) {
        return cartItemRepository.delete(cartItem);
    }
}
