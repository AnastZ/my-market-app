package ru.ya.practicum.payment.api;

import org.junit.jupiter.api.Test;

public class HealthTest extends AbstractTest{

    @Test
    void getHealth_success() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }
}
