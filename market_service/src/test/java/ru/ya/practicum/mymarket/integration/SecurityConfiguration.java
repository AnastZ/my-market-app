package ru.ya.practicum.mymarket.integration;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.config.Customizer.withDefaults;

@TestConfiguration
@EnableReactiveMethodSecurity
public class SecurityConfiguration {
    @Bean
    @Primary
    public SecurityWebFilterChain testSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/items/**").permitAll()
                        .anyExchange().authenticated()
                )
                .formLogin(withDefaults())
                .build();
    }

    @Bean
    @Primary
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username(AbstractTest.username)
                .password("{noop}password")
                .roles("CLIENT")
                .build();
        return new MapReactiveUserDetailsService(user);
    }
    @Bean
    @Primary
    public ReactiveClientRegistrationRepository reactiveClientRegistrationRepository() {
        return Mockito.mock(ReactiveClientRegistrationRepository.class);
    }

    @Bean
    @Primary
    public ReactiveOAuth2AuthorizedClientService reactiveOAuth2AuthorizedClientService() {
        return Mockito.mock(ReactiveOAuth2AuthorizedClientService.class);
    }

    @Bean
    @Primary
    public ReactiveOAuth2AuthorizedClientManager getAuth2AuthorizedClientManager() {
        return Mockito.mock(ReactiveOAuth2AuthorizedClientManager.class);
    }
    @Bean
    public WebClient getPaymentServiceWebClient(final ReactiveOAuth2AuthorizedClientManager manager,
                                                @Value("${payment-service.url}") final String paymentServiceUrl) {

        final ServerOAuth2AuthorizedClientExchangeFilterFunction filter =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(manager);
        filter.setDefaultClientRegistrationId("payment-client");

        return WebClient.builder()
                .baseUrl(paymentServiceUrl)
                .filter(filter)
                .build();
    }

}
