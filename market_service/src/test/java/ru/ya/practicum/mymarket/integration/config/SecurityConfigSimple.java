package ru.ya.practicum.mymarket.integration.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.AuthenticatedPrincipalServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;

@TestConfiguration
public class SecurityConfigSimple {

    @Bean
    @Primary
    public ReactiveOAuth2AuthorizedClientService authorizedClientService(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    @Primary
    public ServerOAuth2AuthorizedClientRepository authorizedClientRepository(
            ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalServerOAuth2AuthorizedClientRepository(authorizedClientService);
    }
    @Bean
    @Primary
    public ReactiveClientRegistrationRepository clientRegistrationRepository() {
        return Mockito.mock(ReactiveClientRegistrationRepository.class);
    }
}
