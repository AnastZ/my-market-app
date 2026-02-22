package ru.yandex.practicum.mymarket.services;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.controllers.dto.CartDTO;
import ru.yandex.practicum.mymarket.controllers.dto.ItemDTO;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.repositories.CartRepository;

import java.util.List;
import java.util.Optional;

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
     * @param sessionId уникальный номер сессии.
     * @return корзина, существующая в БД.
     */
    @Transactional
    public Cart getOrCreateBySessionId(@NotNull final String sessionId) {
        final Optional<Cart> existCart = cartRepository.findBySessionId(sessionId);
        if(existCart.isPresent()) {
            return existCart.get();
        }
        final Cart cart = new Cart(sessionId);
        return cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public CartDTO getCartBySessionId(@NotNull final String sessionId) {
        final List<ItemDTO> items = itemService.findAll(sessionId);
        final Long total = items.stream()
                .map(ItemDTO::getPrice)
                .reduce(Long::sum)
                .orElse(0L);
        return new CartDTO(items, total);
    }

    public void incrementItem(@NotNull final Long itemId,
                              @NotNull @NotBlank final String sessionId){
        itemService.incrementItem(itemId, sessionId);
    }
    public void decrementItem(@NotNull final Long itemId,
                              @NotNull @NotBlank final String sessionId){
        itemService.decrementItem(itemId, sessionId);
    }
}
