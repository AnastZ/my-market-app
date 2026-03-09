package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.CartRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;

public class BuyControllerTest extends AbstractController implements FillCart {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    private final String path = "/buy";

    @Test
    public void buy_success() throws Exception {
        fillDb(sessionId, cartRepository, itemRepository, cartItemRepository)
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
    }
}
