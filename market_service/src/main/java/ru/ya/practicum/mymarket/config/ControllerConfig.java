package ru.ya.practicum.mymarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.ya.practicum.mymarket.controllers.ItemController;

import javax.swing.plaf.basic.BasicComboBoxUI;
import java.net.URI;

@Configuration
public class ControllerConfig {
    @Bean
    public RouterFunction<ServerResponse> itemRoutes() {
        return RouterFunctions.route()
                .GET("/", request -> {
                    String query = request.uri().getQuery();
                    String redirectUri = "/items" + (query != null ? "?" + query : "");
                    return ServerResponse.permanentRedirect(URI.create(redirectUri)).build();
                })
                .build();
    }
}
