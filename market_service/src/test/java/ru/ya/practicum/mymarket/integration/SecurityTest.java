package ru.ya.practicum.mymarket.integration;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import ru.ya.practicum.mymarket.integration.config.SecurityConfig;
import ru.ya.practicum.mymarket.integration.config.SecurityConfigSimple;

@Import(SecurityConfigSimple.class)
public class SecurityTest extends AbstractTestWithRedis{

    @Test
    @WithAnonymousUser
    public void loginPage() throws Exception{
        webTestClient.get()
                .uri("/login")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html")
                .expectBody()
                .consumeWith(result -> {
                });
    }
    @Test
    @WithMockUser
    public void logoutPage_authenticated() throws Exception{
        webTestClient.post()
                .uri("/logout")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .consumeWith(result -> {
                });
    }

}
