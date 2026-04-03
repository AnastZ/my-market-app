package ru.ya.practicum.mymarket.integration.controllers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import ru.ya.practicum.mymarket.integration.AbstractTestWithRedis;
import ru.ya.practicum.mymarket.integration.config.Config;
import ru.ya.practicum.mymarket.integration.FillCart;
import ru.ya.practicum.mymarket.repositories.CartItemRepository;
import ru.ya.practicum.mymarket.repositories.CartRepository;
import ru.ya.practicum.mymarket.repositories.ItemRepository;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.testcontainers.DockerClientFactory.SESSION_ID;

@Import(Config.class)
public class CartControllerTest extends AbstractTestWithRedis implements FillCart {

    private final String path = "/cart/items";

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CartItemRepository cartItemRepository;


    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        FillCart.super
                .fillDb(username, cartRepository, itemRepository, cartItemRepository)
                .collectList()
                .block();
    }

    @Test
    public void getItems_success() {


        final String result = webTestClient.get()
                .uri(path)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML_VALUE)
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        final Document doc = Jsoup.parse(result);

        final Elements cards = doc.select(".card");
        assertNotNull(cards);

        if (!cards.isEmpty()) {
            final Element firstCard = cards.first();

            final Element idInput = firstCard.selectFirst("input[name=id]");
            assertThat(idInput).isNotNull();
            assertThat(idInput.val()).isNotEmpty();

            final Element img = firstCard.selectFirst("img");
            assertThat(img).isNotNull();
            assertThat(img.attr("src")).startsWith("/");

            final Element title = firstCard.selectFirst("h5.card-title");
            assertThat(title).isNotNull();
            assertThat(title.text()).isNotEmpty();

            final Element priceBadge = firstCard.selectFirst("span.badge");
            assertThat(priceBadge).isNotNull();
            assertThat(priceBadge.text()).contains("руб.");

            final Element description = firstCard.selectFirst("p.card-text");
            assertThat(description).isNotNull();
            assertThat(description.text()).isNotEmpty();

            final Element countSpan = firstCard.selectFirst(".hstack span:not(.badge)");
            assertThat(countSpan).isNotNull();
            assertThat(countSpan.text()).matches("\\d+");
        }
    }

    @Test
    void getCartBySessionId_success() {

        webTestClient.get()
                .uri(path)
                .cookie("SESSION", SESSION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    final Document doc = Jsoup.parse(new String(response.getResponseBody(), StandardCharsets.UTF_8));
                    final Element button = doc.select("button[class='btn btn-warning ms-auto']").first();
                    assertThat(button).isNotNull();
                });
    }
}
