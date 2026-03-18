package ru.ya.practicum.payment.api;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class BalanceController extends BalanceApiController {

    @Override
    public Mono<ResponseEntity<Long>> getBalance(String sessionId,
                                                 ServerWebExchange exchange) {
        return super.getBalance(sessionId, exchange);
    }
}
