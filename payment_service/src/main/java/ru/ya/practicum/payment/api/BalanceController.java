package ru.ya.practicum.payment.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.function.Supplier;

@RestController
public class BalanceController implements BalanceApi {

    private final Random random = new Random();
    private final long minBalance;
    private final long maxBalance;

    protected BalanceController(@Value("${balance.min}") final long minBalance,
                                @Value("${balance.max}") final long maxBalance) {
        this.minBalance = minBalance;
        this.maxBalance = maxBalance;
    }

    private Supplier<Long> randomBalance() {
       return ()->random.nextLong(maxBalance - minBalance + 1) + minBalance;
    }
    /**
     * Получить баланс.
     * @param sessionId Идентификатор сессии (required)
     * @param exchange объект для доступа к запросу и ответу
     * @return случайное значение в диапазоне от {@link #minBalance} до {@link #maxBalance}
     */
    @Override
    public Mono<ResponseEntity<Long>> getBalance(final String sessionId,
                                                 final ServerWebExchange exchange) {
        return Mono.fromSupplier(()->ResponseEntity
                .ok()
                .body(randomBalance().get()));
    }

    /**
     * Получить баланс после вычета суммы заказа.
     * @param sessionId Идентификатор сессии (required)
     * @param body сумма заказа в теле запроса (required)
     * @param exchange объект для доступа к запросу и ответу
     * @return баланс после вычета суммы заказа (баланс является случайным числом).
     */
    @Override
    public Mono<ResponseEntity<Long>> payment(final String sessionId,
                                              final Mono<Long> body,
                                              final ServerWebExchange exchange) {
        return body.map((orderAmount)->ResponseEntity
                .ok()
                .body(randomBalance().get() - orderAmount));
    }
}
