package ru.ya.practicum.payment.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

class BalanceControllerTests extends AbstractTest{

    @Value("${balance.min}")
    private long min;

    @Value("${balance.max}")
    private long max;



    @Test
    void getBalance_success() {
        webTestClient.get()
                .uri("/balance/{sessionId}", "sessionId")
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
    void payment_success_positiveBalance(){
        webTestClient.post()
                .uri("/payment/{sessionId}", "sessionId")
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
    void payment_success_negativeBalance(){
        webTestClient.post()
                .uri("/payment/{sessionId}", "sessionId")
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
