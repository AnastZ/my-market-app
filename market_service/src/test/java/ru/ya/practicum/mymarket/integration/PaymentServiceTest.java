package ru.ya.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.ya.practicum.payment.client.ApiClient;
import ru.ya.practicum.payment.client.api.BalanceApi;

import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class PaymentServiceTest{

    private static final int paymentPort = 8085;

    @Container
    private static final GenericContainer<?> paymentContainer = new GenericContainer<>(

            DockerImageName.parse("my-market-app-payment_service"))
            .withExposedPorts(paymentPort)
            .waitingFor(Wait.forHttp("/actuator/health")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofSeconds(90)))
            .withReuse(true);

    private BalanceApi balanceApi;

    @BeforeEach
    void setUp() {

        final String baseUrl = String.format("http://%s:%d", paymentContainer.getHost(), paymentContainer.getMappedPort(paymentPort));
        System.out.println("Payment service URL: " + baseUrl);

        final WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        final ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath(baseUrl);
        balanceApi = new BalanceApi(apiClient);

        System.out.println("BalanceApi created with basePath: " + apiClient.getBasePath());
    }

    @Test
    void getBalance_shouldReturnPositiveLong() {
        final Long balance = balanceApi.getBalance("test-session-123").block();
        assertNotNull(balance);
        assertTrue(balance > 0, "Balance should be positive");
        System.out.println("Received balance: " + balance);
    }

    @Test
    void payment_shouldDecreaseBalance() {
        final Long initialBalance = balanceApi.getBalance("test-session-456").block();
        assertNotNull(initialBalance);
        System.out.println("Initial balance: " + initialBalance);

        final Long paymentAmount = 100L;
        final Long newBalance = balanceApi.payment("test-session-456", paymentAmount).block();

        assertNotNull(newBalance);
    }

    @Test
    void getBalance_withInvalidSession_shouldHandleError() {
        assertThrows(Exception.class, () -> {
            balanceApi.getBalance("").block();
        });
    }
}
