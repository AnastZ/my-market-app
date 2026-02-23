package ru.yandex.practicum.mymarket.services;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemService {
    private final CartItemRepository cartItemRepository;

    public CartItemService(@NotNull final CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional(readOnly = true)
    public Optional<CartItem> findById(@NotNull final Long itemId,
                                       @NotNull @NotBlank final String sessionId) {
        return cartItemRepository.findByItemIdAndSessionId(itemId, sessionId);
    }

    @Transactional(readOnly = true)
    public List<CartItem> findItemsBySessionId(@NotNull @NotBlank final String sessionId) {
        return cartItemRepository.getCartItems(sessionId);
    }

    @Transactional
    public CartItem save(@NotNull final CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public void delete(@NotNull final CartItem cartItem) {
        cartItemRepository.delete(cartItem);
    }
}
