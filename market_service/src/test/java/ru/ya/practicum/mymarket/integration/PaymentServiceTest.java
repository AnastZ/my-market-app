package ru.ya.practicum.mymarket.integration;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.ya.practicum.payment.client.ApiClient;
import ru.ya.practicum.payment.client.api.BalanceApi;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class PaymentServiceTest {
    private static final Network NETWORK = Network.newNetwork();
    private static final int paymentPort = 8085;
    @Container
    private static final KeycloakContainer keycloak =
            new KeycloakContainer("quay.io/keycloak/keycloak:22.0.1")
                    .withNetwork(NETWORK)
                    .withNetworkAliases("keycloak")
                    .withRealmImportFile("realm.json")
                    .withEnv("KC_HOSTNAME", "keycloak")
                    .withEnv("KC_HOSTNAME_PORT", "8080")
                    .withEnv("KC_HOSTNAME_STRICT", "false")
                    .waitingFor(Wait.forHttp("/realms/test-realm").forPort(8080).forStatusCode(200));

    @Container
    private static final GenericContainer<?> paymentContainer = new GenericContainer<>(
            DockerImageName.parse("payment_service:1.0"))
            .dependsOn(keycloak)
            .withExposedPorts(paymentPort)
            .withNetwork(NETWORK)
            .withEnv("PAYMENT_PORT", String.valueOf(paymentPort))
            .withEnv("PAYMENT_AUTH_URI", "http://keycloak:8080/realms/test-realm")
            .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200))
            .withReuse(true);

    private BalanceApi balanceApi;
    private static String accessToken;

    private static void obtainAccessToken(){
        paymentContainer.followOutput(outputFrame -> System.out.print("[payment] " + outputFrame.getUtf8String()));

        final String tokenUrl = keycloak.getAuthServerUrl() + "/realms/test-realm/protocol/openid-connect/token";
        final WebClient tokenClient = WebClient.builder().build();

        final TokenResponse response = tokenClient.post()
                .uri(tokenUrl)
                .bodyValue("client_id=payment-client&client_secret=secret&grant_type=password&username=testuser&password=password")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK, resp -> resp.bodyToMono(String.class).map(RuntimeException::new))
                .bodyToMono(TokenResponse.class)
                .block(Duration.ofSeconds(10));

        assertNotNull(response);
        accessToken = response.getAccessToken();
    }

    @BeforeEach
    void setUp() {
        obtainAccessToken();
        final String baseUrl = String.format("http://%s:%d", paymentContainer.getHost(), paymentContainer.getMappedPort(paymentPort));

        final WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build();

        final ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath(baseUrl);
        balanceApi = new BalanceApi(apiClient);
    }

    @Test
    void getBalance_shouldReturnPositiveLong() {
        Long balance = balanceApi.getBalance("test-session-123").block();
        assertNotNull(balance);
        assertTrue(balance > 0, "Balance should be positive");
    }

    @Test
    void payment_shouldDecreaseBalance() {
        final Long initialBalance = balanceApi.getBalance("test-session-456").block();
        assertNotNull(initialBalance);
        final Long paymentAmount = 100L;
        final Long newBalance = balanceApi.payment("test-session-456", paymentAmount).block();
        assertNotNull(newBalance);
    }

    @Test
    void getBalance_withAnyUsername_shouldReturnBalance() {
        final Long balance = balanceApi.getBalance("test").block();
        assertNotNull(balance);
        assertTrue(balance > 0);
    }

    static class TokenResponse {
        private String access_token;

        public String getAccessToken() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }
    }
}
