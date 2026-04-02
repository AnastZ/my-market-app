package ru.ya.practicum.mymarket.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ya.practicum.payment.client.ApiClient;
import ru.ya.practicum.payment.client.api.BalanceApi;

@Configuration
public class PaymentClientConfig {

    @Bean
    public ApiClient paymentApiClient(@NotNull final WebClient paymentWebClient) {
        return new ApiClient(paymentWebClient);
    }
    @Bean
    public BalanceApi balanceApi(@NotNull final ApiClient paymentApiClient) {
        return new BalanceApi(paymentApiClient);
    }
}