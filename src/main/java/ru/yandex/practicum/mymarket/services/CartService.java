package ru.yandex.practicum.mymarket.services;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.controllers.dto.CartDTO;
import ru.yandex.practicum.mymarket.controllers.dto.ItemDTO;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.repositories.CartRepository;

import java.util.List;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private ItemService itemService;

    public CartService(@NotNull final CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public void setItemService(@NotNull final ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Найти существующую корзину в БД по уникальному полю - id сессии
     * или создать новый объект корзины с переданным id сессии.
     *
     * @param sessionId уникальный номер сессии.
     * @return корзина, существующая в БД.
     */
    @Transactional
    public Mono<Cart> getOrCreateBySessionId(@NotNull final String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .switchIfEmpty(cartRepository.save(new Cart(sessionId)));
    }

    @Transactional(readOnly = true)
    public Mono<CartDTO> getCartBySessionId(@NotNull final String sessionId) {
        return itemService.findAllInCart(sessionId)
                .collectList()
                .map(items -> {
                    final Long total = items.stream()
                            .map(ItemDTO::getPrice)
                            .reduce(Long::sum)
                            .orElse(0L);
                    return new CartDTO(items, total);
                });
    }

    public Mono<Void> incrementItem(@NotNull final Long itemId,
                                    @NotNull @NotBlank final String sessionId) {
        return itemService.incrementItem(itemId, sessionId);
    }

    public Mono<Void> decrementItem(@NotNull final Long itemId,
                                    @NotNull @NotBlank final String sessionId) {
        return itemService.decrementItem(itemId, sessionId);
    }
}
