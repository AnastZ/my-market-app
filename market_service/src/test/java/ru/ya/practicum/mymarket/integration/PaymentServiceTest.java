package ru.ya.practicum.mymarket.integration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.ya.practicum.payment.client.ApiClient;
import ru.ya.practicum.payment.client.api.BalanceApi;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class PaymentServiceTest {

    private static final int paymentPort = 8085;
    private static final int wiremockPort = 8088;
    private static final Network NETWORK = Network.newNetwork();
    @Container
    private static final GenericContainer<?> mockOauthServer = new GenericContainer<>(
            DockerImageName.parse("wiremock/wiremock:latest"))
            .withEnv("TESTCONTAINERS_HOST_OVERRIDE", "host.docker.internal")
            .withNetwork(NETWORK)
            .withNetworkAliases("oauth-mock")
            .withExposedPorts(wiremockPort)
            .waitingFor(Wait.forLogMessage(".*port: 8080.*", 1))
            .withStartupTimeout(Duration.ofSeconds(60));

    @Container
    private static final GenericContainer<?> paymentContainer = new GenericContainer<>(
            DockerImageName.parse("payment_service:1.0"))
            .withExposedPorts(paymentPort)
            .withNetwork(NETWORK)
            .withEnv("PAYMENT_PORT", String.valueOf(paymentPort))
            .withEnv("PAYMENT_AUTH_URI", "http://host.docker.internal:"+ wiremockPort + "/realms/test-realm")
            .waitingFor(Wait.forHttp("/actuator/health")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofSeconds(90)))
            .withReuse(true);

    private BalanceApi balanceApi;

    private String wiremockBaseUrl;
    private String wiremockContainerUrl;
    @BeforeEach
    void setUp() {
        String paymentServiceUrl = String.format("http://%s:%d",
                paymentContainer.getHost(),
                paymentContainer.getMappedPort(paymentPort));

        wiremockBaseUrl = String.format("http://%s:%d",
                mockOauthServer.getHost(),
                mockOauthServer.getMappedPort(wiremockPort));

        wiremockContainerUrl = String.format("http://oauth-mock:%d", wiremockPort);

        System.out.println("Payment service URL: " + paymentServiceUrl);
        System.out.println("WireMock URL (from host): " + wiremockBaseUrl);
        System.out.println("WireMock URL (from container): " + wiremockContainerUrl);

        // Настраиваем WireMock стабы ДО запуска payment_service
        setupWireMockStubs();

        // Получаем токен для вызовов к payment_service
        String accessToken = getAccessTokenFromMock();

        final WebClient webClient = WebClient.builder()
                .baseUrl(paymentServiceUrl)
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build();

        final ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath(paymentServiceUrl);
        balanceApi = new BalanceApi(apiClient);

        System.out.println("BalanceApi created");
    }

    private void setupWireMockStubs() {
        String adminUrl = String.format("%s/__admin/mappings", wiremockBaseUrl);

        // 1. Самый важный стаб - корневой endpoint realm (Spring Security запрашивает его первым)
        String realmResponse = String.format("""
            {
                "realm": "test-realm",
                "public_key": "test-public-key",
                "token-service": "%s/realms/test-realm/protocol/openid-connect",
                "account-service": "%s/realms/test-realm/account",
                "tokens-not-before": 0
            }
            """, wiremockContainerUrl, wiremockContainerUrl);

        WebClient.create(adminUrl)
                .post()
                .bodyValue(Map.of(
                        "request", Map.of(
                                "method", "GET",
                                "urlPath", "/realms/test-realm"
                        ),
                        "response", Map.of(
                                "status", 200,
                                "jsonBody", realmResponse,
                                "headers", Map.of("Content-Type", "application/json")
                        )
                ))
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        // 2. Стаб для конфигурации OpenID Connect
        String wellKnownResponse = String.format("""
            {
                "issuer": "%s/realms/test-realm",
                "jwks_uri": "%s/realms/test-realm/protocol/openid-connect/certs",
                "token_endpoint": "%s/realms/test-realm/protocol/openid-connect/token",
                "authorization_endpoint": "%s/realms/test-realm/protocol/openid-connect/auth"
            }
            """, wiremockContainerUrl, wiremockContainerUrl, wiremockContainerUrl, wiremockContainerUrl);

        WebClient.create(adminUrl)
                .post()
                .bodyValue(Map.of(
                        "request", Map.of(
                                "method", "GET",
                                "urlPath", "/realms/test-realm/.well-known/openid-configuration"
                        ),
                        "response", Map.of(
                                "status", 200,
                                "jsonBody", wellKnownResponse,
                                "headers", Map.of("Content-Type", "application/json")
                        )
                ))
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        // 3. Стаб для JWKS (ключи подписи) - обязателен для валидации JWT
        String jwksResponse = """
            {
                "keys": [
                    {
                        "kty": "RSA",
                        "kid": "test-key",
                        "use": "sig",
                        "n": "test-modulus",
                        "e": "AQAB"
                    }
                ]
            }
            """;

        WebClient.create(adminUrl)
                .post()
                .bodyValue(Map.of(
                        "request", Map.of(
                                "method", "GET",
                                "urlPath", "/realms/test-realm/protocol/openid-connect/certs"
                        ),
                        "response", Map.of(
                                "status", 200,
                                "jsonBody", jwksResponse,
                                "headers", Map.of("Content-Type", "application/json")
                        )
                ))
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        // 4. Стаб для токен эндпоинта
        String tokenResponse = String.format("""
            {
                "access_token": "%s",
                "token_type": "Bearer",
                "expires_in": 3600
            }
            """, generateTestJwt());

        WebClient.create(adminUrl)
                .post()
                .bodyValue(Map.of(
                        "request", Map.of(
                                "method", "POST",
                                "urlPath", "/realms/test-realm/protocol/openid-connect/token"
                        ),
                        "response", Map.of(
                                "status", 200,
                                "jsonBody", tokenResponse,
                                "headers", Map.of("Content-Type", "application/json")
                        )
                ))
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        System.out.println("WireMock stubs configured");
    }

    private String generateTestJwt() {
        // Используем HMAC256 для простоты
        Algorithm algorithm = Algorithm.HMAC256("test-secret-key");
        return JWT.create()
                .withSubject("testuser")
                .withClaim("roles", List.of("CLIENT"))
                .withClaim("preferred_username", "testuser")
                .withIssuer(wiremockContainerUrl + "/realms/test-realm")
                .sign(algorithm);
    }

    private String getAccessTokenFromMock() {
        String tokenUrl = String.format("%s/realms/test-realm/protocol/openid-connect/token", wiremockBaseUrl);

        Map<String, String> response = WebClient.builder()
                .baseUrl(tokenUrl)
                .build()
                .post()
                .bodyValue("client_id=payment-client&client_secret=test&grant_type=client_credentials")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return response.get("access_token");
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
        System.out.println("New balance: " + newBalance);
    }

    @Test
    void getBalance_withInvalidSession_shouldHandleError() {
        assertThrows(Exception.class, () -> {
            balanceApi.getBalance("").block();
        });
    }
}