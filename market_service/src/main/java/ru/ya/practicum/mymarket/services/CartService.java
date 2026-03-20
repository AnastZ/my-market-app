package ru.ya.practicum.mymarket.services;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.controllers.dto.CartDTO;
import ru.ya.practicum.mymarket.controllers.dto.ItemDTO;
import ru.ya.practicum.mymarket.model.Cart;
import ru.ya.practicum.mymarket.repositories.CartRepository;
import ru.ya.practicum.payment.client.api.BalanceApi;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.function.Function;

@Service
public class CartService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final CartRepository cartRepository;
    private ItemService itemService;
    private final BalanceApi balanceApi;
    private final PaymentServiceHealthChecker paymentServiceHealthChecker;

    public CartService(@NotNull final CartRepository cartRepository,
                       @NotNull final BalanceApi balanceApi,
                       @NotNull final PaymentServiceHealthChecker paymentServiceHealthChecker) {
        this.cartRepository = cartRepository;
        this.balanceApi = balanceApi;
        this.paymentServiceHealthChecker = paymentServiceHealthChecker;
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
                .switchIfEmpty(Mono.defer(() -> cartRepository.save(new Cart(sessionId))));
    }

    /**
     * Получить объект корзины с товарами по уникальному номеру сессии.
     *
     * @param sessionId уникальный номер сессии.
     * @return объект корзины с товарами
     */
    @Transactional(readOnly = true)
    public Mono<CartDTO> getCartBySessionId(@NotNull final String sessionId) {
        return paymentServiceHealthChecker.isHealthy()
                .flatMap(healthy -> {
                    if (healthy) {
                        return balanceApi.getBalance(sessionId)
                                .flatMap(balance -> loadCartsBySessionId(sessionId, t -> t <= balance));
                    } else {
                        log.error("Payment service is not available.");
                        return loadCartsBySessionId(sessionId, t -> false);
                    }
                });
    }

    private Mono<CartDTO> loadCartsBySessionId(@NotNull final String sessionId,
                                               final Function<Long, Boolean> successBuy) {
        return itemService.findAllInCart(sessionId)
                .collectList()
                .map(items -> {
                    final Long total = items.stream()
                            .map(ItemDTO::getPrice)
                            .reduce(Long::sum)
                            .orElse(0L);
                    return new CartDTO(items, total, successBuy.apply(total));
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
