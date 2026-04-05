package ru.ya.practicum.mymarket.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.repositories.UserRepository;
import ru.ya.practicum.mymarket.repositories.UserRoleRepository;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity(securedEnabled = true)
@Profile("!test")
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(@NotNull final UserRepository userRepository,
                                                         @NotNull final UserRoleRepository userRoleRepository) {
        return username -> userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)))
                .flatMap(user -> userRoleRepository.findByUsername(username)
                        .collectList()
                        .map(roles -> {
                            final List<SimpleGrantedAuthority> authorities = roles.stream()
                                    .map(userRole -> new SimpleGrantedAuthority(userRole.getRole()))
                                    .collect(Collectors.toList());
                            return org.springframework.security.core.userdetails.User
                                    .withUsername(user.getUsername())
                                    .password(user.getPassword())
                                    .authorities(authorities)
                                    .build();
                        }));
    }

    @Bean
    public ReactiveUserDetailsPasswordService reactiveUserDetailsPasswordService(
            @NotNull final UserRepository userRepository,
            @NotNull final PasswordEncoder passwordEncoder) {

        return (user, password) -> userRepository.findByUsername(user.getUsername())
                .flatMap(dbUser -> {
                    dbUser.setPassword(passwordEncoder.encode(password));
                    return userRepository.save(dbUser);
                })
                .map(updatedUser -> org.springframework.security.core.userdetails.User
                        .withUsername(updatedUser.getUsername())
                        .password(updatedUser.getPassword())
                        .authorities(user.getAuthorities())
                        .build());
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((exchange, authentication) ->
                                exchange.getExchange().getSession()
                                        .flatMap(WebSession::invalidate)
                                        .doOnSuccess(v -> {
                                            ServerHttpResponse response = exchange.getExchange().getResponse();
                                            response.addCookie(ResponseCookie.from("JSESSIONID", "")
                                                    .path("/")
                                                    .maxAge(Duration.ZERO)
                                                    .httpOnly(true)
                                                    .build());
                                            response.addCookie(ResponseCookie.from("remember-me", "")
                                                    .path("/")
                                                    .maxAge(Duration.ZERO)
                                                    .build());
                                            response.setStatusCode(HttpStatus.OK);
                                        })
                                        .then()
                        ))
                .oauth2Login(Customizer.withDefaults())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/items/**").permitAll()
                        .anyExchange().authenticated()
                )
                .build();
    }

    @Bean
    public WebClient paymentServiceWebClient(final ReactiveOAuth2AuthorizedClientManager manager,
                                             @Value("${payment-service.url}") final String paymentServiceUrl) {

        final ServerOAuth2AuthorizedClientExchangeFilterFunction filter =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(manager);
        filter.setDefaultClientRegistrationId("payment-client");

        return WebClient.builder()
                .baseUrl(paymentServiceUrl)
                .filter(filter)
                .build();
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientManager auth2AuthorizedClientManager(
            final ReactiveClientRegistrationRepository clientRegistrationRepository,
            final ReactiveOAuth2AuthorizedClientService authorizedClientService) {

        final AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);

        manager.setAuthorizedClientProvider(ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .refreshToken()
                .build()
        );
        return manager;
    }

}
