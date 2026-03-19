package ru.ya.practicum.mymarket.services;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class PaymentServiceHealthChecker {
    private final WebClient webClient;

    public PaymentServiceHealthChecker(final @NotNull WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Проверяет, доступен ли сервис платежей через actuator/health.
     * @return Mono<Boolean> true – сервис доступен, false – недоступен
     */
    public @NotNull Mono<Boolean> isHealthy() {
        return webClient.get()
                .uri("/actuator/health")
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(HealthResponse.class)
                                .map(health -> "UP".equals(health.getStatus()));
                    } else {
                        return Mono.just(false);
                    }
                })
                .timeout(Duration.ofSeconds(2))
                .onErrorReturn(false);
    }

    /** Внутренний класс для парсинга ответа health
     *
     */
    private static class HealthResponse {
        private String status;
        public @NotNull String getStatus() { return status; }
        public void setStatus(final @NotNull String status) { this.status = status; }
    }
}
