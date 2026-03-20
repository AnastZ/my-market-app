package ru.ya.practicum.mymarket.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.controllers.dto.OrderDTO;
import ru.ya.practicum.mymarket.services.OrderService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


public class BuyControllerTest extends AbstractTest implements FillCart {

    @MockitoBean
    private OrderService orderService;

    @Test
    public void buy_success() throws Exception {

        final OrderDTO orderDTO = new OrderDTO(1L, Collections.emptyList(), 100L);

        when(orderService.createOrder(anyString()))
                .thenReturn(Mono.just(orderDTO));

        webTestClient.post()
                .uri("/buy")
                .contentType(MediaType.TEXT_HTML)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader()
                .valueMatches(HttpHeaders.LOCATION, "orders/.*");
    }
}
