package ru.ya.practicum.mymarket.integration.config;

import jakarta.validation.constraints.NotNull;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.AuthenticatedPrincipalServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.controllers.dto.EntityConvertor;
import ru.ya.practicum.mymarket.model.Item;
import ru.ya.practicum.mymarket.model.ItemWithCartCount;
import ru.ya.practicum.mymarket.services.PaymentServiceHealthChecker;
import ru.ya.practicum.payment.client.api.BalanceApi;

@TestConfiguration
public class Config {

    @Bean
    @Primary
    public BalanceApi mockBalanceApi() {
        return new BalanceApi() {
            @Override
            public Mono<Long> getBalance(String username) {
                return Mono.just(100_000_000L);
            }

            @Override
            public Mono<Long> payment(String username, Long body) {
                return Mono.just(1_000L);
            }
        };
    }

    @Bean
    @Primary
    public PaymentServiceHealthChecker mockHealthChecker(@NotNull final WebClient webClient) {
        return new PaymentServiceHealthChecker(webClient) {
            @Override
            public @NotNull Mono<Boolean> isHealthy() {
                return Mono.just(true);
            }
        };
    }
    @Bean
    public EntityConvertor<ItemWithCartCount, Item> getItemConvertor() {
        return entity -> {
            final Item i = new Item(entity.getTitle(), entity.getDescription(), entity.getImgPath(), entity.getPrice());
            i.setId(entity.getId());
            return i;
        };
    }


}
