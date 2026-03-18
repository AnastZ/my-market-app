package ru.yandex.practicum.mymarket.integration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.CartRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;

import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class CartControllerTest extends AbstractController implements FillCart{

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
}
