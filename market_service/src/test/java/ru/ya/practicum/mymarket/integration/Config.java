package ru.ya.practicum.mymarket.integration;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.repositories.OrderItemRepository;
import ru.ya.practicum.mymarket.services.PaymentServiceHealthChecker;
import ru.ya.practicum.payment.client.api.BalanceApi;

@TestConfiguration

public class Config {

    @Bean
    @Primary
    public BalanceApi mockBalanceApi() {
        return new BalanceApi() {
            @Override
            public Mono<Long> getBalance(String sessionId) {
                return Mono.just(10000L); // Всегда возвращаем 10000
            }

            @Override
            public Mono<Long> payment(String sessionId, Long body) {
                return Mono.just(10000L - body); // Всегда успешный платёж
            }
        };
    }

    @Bean
    @Primary
    public PaymentServiceHealthChecker mockHealthChecker(WebClient webClient) {
        return new PaymentServiceHealthChecker(webClient) {
            @Override
            public Mono<Boolean> isHealthy() {
                return Mono.just(true); // Всегда здоров
            }
        };
    }

}
