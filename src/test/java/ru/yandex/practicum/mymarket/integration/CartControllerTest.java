package ru.yandex.practicum.mymarket.integration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.CartRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;


public class CartControllerTest extends AbstractController implements FillCart{

    private final String path = "/cart/items";

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CartItemRepository cartItemRepository;


    @Test
    public void getItems_success() throws Exception {

        final Mono<Void> testChain = FillCart.super
                .fillDb(sessionId, cartRepository, itemRepository, cartItemRepository)
                        .collectList()
                                .then(Mono.fromRunnable(()->{
                                    webTestClient.get().uri(path)
                                            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                                            .cookie("SESSION", sessionId)
                                            .exchange()
                                            .expectStatus().isOk()
                                            .expectHeader().contentType(MediaType.TEXT_HTML_VALUE + ";charset=utf-8")
                                            .expectBody(String.class)
                                            .consumeWith(result -> {
                                                assertNotNull(result.getResponseBody());
                                                final Document doc = Jsoup.parse(result.getResponseBody());

                                                final Elements cards = doc.select(".card");
                                                assertNotNull(cards);
                                                final Element firstCard = cards.first();

                                                final Element idInput = firstCard.selectFirst("input[name=id]");
                                                assertThat(idInput).isNotNull();
                                                assertThat(idInput.val()).isNotEmpty();

                                                final Element img = firstCard.selectFirst("img.card-img-top");
                                                assertThat(img).isNotNull();
                                                assertThat(img.attr("src")).startsWith("/");

                                                final Element title = firstCard.selectFirst(".card-title");
                                                assertThat(title).isNotNull();
                                                assertThat(title.text()).contains("title");

                                                final Element priceBadge = firstCard.selectFirst(".badge");
                                                assertThat(priceBadge).isNotNull();
                                                assertThat(priceBadge.text()).contains("руб.");

                                                final Element description = firstCard.selectFirst(".card-text");
                                                assertThat(description).isNotNull();
                                                assertThat(description.text()).isNotEmpty();

                                                final Element countSpan = firstCard.selectFirst(".hstack span");
                                                assertThat(countSpan).isNotNull();
                                                assertThat(countSpan.text()).matches("\\d+");
                                            });
                                }));
        StepVerifier.create(testChain).verifyComplete();
    }
    @Test
    public void getEmptyItems_success() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        mockMvc.perform(get(path)
                        .session(session)
                        .contentType(MediaType.TEXT_HTML)
                        .accept(MediaType.TEXT_HTML)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("items", is(empty())))
                .andExpect(model().attribute("total", is(0L)));
    }

    @ParameterizedTest
    @MethodSource("itemIds")
    public void changeItem_success(final Long itemId) throws Exception {
        final MockHttpSession session = new MockHttpSession();
        FillCart.super.fillDb(session, cartRepository, itemRepository, cartItemRepository);

        final Optional<CartItem> beforeItem = cartItemRepository.findByItemIdAndSessionId(itemId, session.getId());
        if (beforeItem.isEmpty())
            return;
        mockMvc.perform(post(path)
                        .session(session)
                        .param("id", itemId.toString())
                        .param("action", "PLUS")
                        .contentType(MediaType.TEXT_HTML)
                        .accept(MediaType.TEXT_HTML)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("items", hasItem( allOf(
                        hasProperty("id"),
                        hasProperty("title"),
                        hasProperty("description"),
                        hasProperty("imgPath"),
                        hasProperty("price"),
                        hasProperty("count", is(not(0L))))
                )));
        assertTrue(beforeItem.get().getCount() < cartItemRepository.findByItemIdAndSessionId(itemId, session.getId()).get().getCount());

    }
}
