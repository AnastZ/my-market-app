package ru.ya.practicum.mymarket.integration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.repositories.CartItemRepository;
import ru.ya.practicum.mymarket.repositories.CartRepository;
import ru.ya.practicum.mymarket.repositories.ItemRepository;
import ru.ya.practicum.mymarket.services.PaymentServiceHealthChecker;
import ru.ya.practicum.payment.client.api.BalanceApi;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.testcontainers.DockerClientFactory.SESSION_ID;


public class CartControllerTest extends AbstractTest implements FillCart {

    private final String path = "/cart/items";

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CartItemRepository cartItemRepository;


    @Test
    public void getItems_success() {
        final Consumer<String> action = result -> {

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
        };

        FillCart.super
                .fillDb(sessionId, cartRepository, itemRepository, cartItemRepository)
                .collectList()
                .block();

        final String html = webTestClient.get()
                .uri(path)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML_VALUE)
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        action.accept(html);
    }

    @MockitoBean
    private BalanceApi balanceApi;

    @MockitoBean
    private PaymentServiceHealthChecker healthChecker;

    @Test
    void getCartBySessionId_success() {
        FillCart.super
                .fillDb(sessionId, cartRepository, itemRepository, cartItemRepository)
                .collectList()
                .block();

        when(healthChecker.isHealthy()).thenReturn(Mono.just(true));
        when(balanceApi.getBalance(anyString())).thenReturn(Mono.just(1500_000L));

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
