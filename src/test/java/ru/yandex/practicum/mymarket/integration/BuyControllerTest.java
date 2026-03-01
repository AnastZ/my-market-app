package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.CartRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class BuyControllerTest extends AbstractController implements FillCart{
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    private final String path = "/buy";
    @Test
    public void buy_success() throws Exception {


        final Mono<Void> testChain = fillDb(sessionId, cartRepository, itemRepository, cartItemRepository)
                .collectList()
                .then(Mono.fromRunnable(() -> {
                    webTestClient.post()
                            .uri("/buy")
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                            .cookie("SESSION", sessionId)
                            .exchange()
                            .expectStatus().is3xxRedirection()
                            .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/orders/.*");
                }));
        StepVerifier.create(testChain)
                .verifyComplete();

    }
}
