package ru.ya.practicum.payment.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BalanceControllerTests extends AbstractTest {

    @Value("${balance.min}")
    private long min;

    @Value("${balance.max}")
    private long max;

    @Test
    @WithMockUser(authorities = {"CLIENT"})
    void getBalance_success() {
        webTestClient
                .get()
                .uri("/balance/{username}", "username")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Long.class)
                .consumeWith(response -> {
                    final Long balance = response.getResponseBody();
                    assertNotNull(balance);
                    assertTrue(balance >= min && balance <= max,
                            "Balance should be between " + min + " and " + max);
                });
    }

    @Test
    @WithMockUser(authorities = {"CLIENT"})
    void payment_success_positiveBalance() {
        webTestClient
                .post()
                .uri("/payment/{username}", "username")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(min - 1L)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Long.class)
                .consumeWith(response -> {
                    final Long balanceAfterPayment = response.getResponseBody();
                    assertNotNull(balanceAfterPayment);
                    assertTrue(balanceAfterPayment > 0);
                });
    }

    @Test
    @WithMockUser(authorities = {"CLIENT"})
    void payment_success_negativeBalance() {
        webTestClient.post()
                .uri("/payment/{username}", "username")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(max - 1L)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Long.class)
                .consumeWith(response -> {
                    final Long balanceAfterPayment = response.getResponseBody();
                    assertNotNull(balanceAfterPayment);
                    assertFalse(balanceAfterPayment > 0);
                });
    }
}
