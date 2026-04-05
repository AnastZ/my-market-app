package ru.ya.practicum.payment.api;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.HttpStatusReturningServerLogoutSuccessHandler;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
 
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtSpec -> {
                    final ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
                    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
                        List<String> roles = jwt.getClaim("roles");
                        if (Objects.isNull(roles) || roles.isEmpty()) {
                            final Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                            if (Objects.nonNull(realmAccess) && realmAccess.containsKey("roles")) {
                                roles = (List<String>) realmAccess.get("roles");
                            }
                        }
                        if (Objects.isNull(roles) || roles.isEmpty()) {
                            return Flux.empty();
                        }
                        return Flux.fromIterable(roles)
                                .map(SimpleGrantedAuthority::new);
                    });
                    jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter);
                }))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .anyExchange().authenticated()
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(new HttpStatusReturningServerLogoutSuccessHandler())
                )
                .build();
    }

}
