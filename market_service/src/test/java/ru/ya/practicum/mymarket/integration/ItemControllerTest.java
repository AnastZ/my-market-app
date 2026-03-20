package ru.ya.practicum.mymarket.integration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.controllers.ItemController;
import ru.ya.practicum.mymarket.model.CartItem;
import ru.ya.practicum.mymarket.repositories.CartItemRepository;
import ru.ya.practicum.mymarket.repositories.ItemRepository;
import ru.ya.practicum.mymarket.services.ItemService;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
public class ItemControllerTest extends AbstractTest implements FillItems {

    static final String path = "/items";

    @Autowired
    private ItemRepository itemRepository;

    @Value("${item.list-size}")
    private int itemListSize;


    private int getCountItems() throws IllegalArgumentException {
        final int count = itemRepository.findAllWithCart("", "", Sort.unsorted())
                .collectList()
                .map(List::size)
                .block()
                .intValue();
        if (count == 0) throw new IllegalArgumentException("No items found.");
        return count;
    }

    private void checkSearch(final Document doc) {
        assertThat(doc.select("input[name='search']").val()).isEqualTo("");
    }

    private void checkSort(final Document doc, final ItemController.SortMethod sortMethod) {
        final Element sortSelect = doc.select("select[id='sort']").first();
        assertThat(sortSelect).isNotNull();
        final Element firstOption = sortSelect.select("option[selected]").first();
        assertThat(firstOption).isNotNull();
        assertThat(firstOption.val()).isEqualTo(sortMethod.toString());
    }

    private void checkPageNumber(final Document doc, final int pageNumber) {
        final Element pageSpan = doc.select("span:contains(Страница:)").first();
        assertThat(pageSpan).isNotNull();
        assertThat(pageSpan.text()).contains(String.valueOf(pageNumber));
    }

    private void checkPageSize(final Document doc, final int pageSize) {
        final Element selectedPageSize = doc.select("select[id='pageSize'] option[selected]").first();
        assertThat(selectedPageSize).isNotNull();
        assertThat(selectedPageSize.val()).isEqualTo(String.valueOf(pageSize));
    }

    private void checkPaging(final Document doc, final int pageNumber, final int countPages) {
        final Element prevButton = doc.select("button[name='pageNumber'][value='" + (pageNumber - 1) + "']").first();
        if (pageNumber == 1) {
            assertThat(prevButton).isNull();
        } else {
            assertThat(prevButton).isNotNull();
        }

        final Element nextButton = doc.select("button[name='pageNumber'][value='" + (pageNumber + 1) + "']").first();
        if (pageNumber < countPages) {
            assertThat(nextButton).isNotNull();
        } else {
            assertThat(nextButton).isNull();
        }
    }

    private void checkItemList(final Document doc) {
        assertThat(doc.select("div.card")).isNotEmpty();

        final Elements cardRows = doc.select("div.row.p-2:has(div.col div.card)");
        if (!cardRows.isEmpty()) {
            final Elements colsInFirstRow = cardRows.first().select("> div.col");
            assertThat(colsInFirstRow).hasSize(itemListSize);

            final Element lastCol = colsInFirstRow.last();
            if (lastCol.select("div.card").isEmpty()) {
                assertThat(lastCol.html()).contains("&nbsp;");
            }
        }
    }

    private void checkItems(final Document doc,
                            final int pageSize,
                            final int pageNum,
                            final int countPages,
                            final ItemController.SortMethod method) {
        checkSearch(doc);
        checkSort(doc, method);
        checkPageSize(doc, pageSize);
        checkItemList(doc);
        checkPageNumber(doc, pageNum);
        checkPaging(doc, pageNum, countPages);
    }

    @Test
    public void findAll_withoutParams() {
        final int defNumber = 1;
        final int defSize = 5;
        final int countPages = (int) Math.ceil((double) getCountItems() / defSize);
        webTestClient.get()
                .uri(path)
                .cookie("SESSION", sessionId)
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html")
                .expectBody()
                .consumeWith(result -> {
                    final Document doc = Jsoup.parse(new String(result.getResponseBody(), StandardCharsets.UTF_8));
                    checkItems(doc, defSize, defNumber, countPages, ItemController.SortMethod.NO);
                });
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 5", "2, 10"})
    public void findAll_withoutSearchAndSort(final int pageNumber, final int pageSize) {
        final int countPages = (int) Math.ceil((double) getCountItems() / pageSize);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("pageNumber", pageNumber)
                        .queryParam("pageSize", pageSize)
                        .build())
                .cookie("SESSION", sessionId)
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html")
                .expectBody()
                .consumeWith(result -> {

                    Document doc = Jsoup.parse(new String(result.getResponseBody(), StandardCharsets.UTF_8));
                    checkItems(doc, pageSize, pageNumber, countPages, ItemController.SortMethod.NO);
                });
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 5, NO", "2, 10, ALPHA", "2, 5, PRICE"})
    public void findAll_withSort(final int pageNumber,
                                 final int pageSize,
                                 final ItemController.SortMethod method) {


        final int countPages = (int) Math.ceil((double) getCountItems() / pageSize);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("pageNumber", pageNumber)
                        .queryParam("pageSize", pageSize)
                        .queryParam("sort", method.toString())
                        .build())
                .cookie("SESSION", sessionId)
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html")
                .expectBody()
                .consumeWith(result -> {
                    final Document doc = Jsoup.parse(new String(result.getResponseBody(), StandardCharsets.UTF_8));
                    checkItems(doc, pageSize, pageNumber, countPages, method);
                });
    }


    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemService itemService;

    @TestFactory
    public Stream<DynamicTest> increment_success() {
        return itemRepository.findAll()
                .filter(Objects::nonNull)
                .take(10)
                .flatMap(item ->
                        Mono.just(DynamicTest.dynamicTest(
                                "Incrementing item with ID: " + item.getId(),
                                () -> {
                                    itemService.incrementItem(item.getId(), sessionId).block();

                                    final Long beforeCount = cartItemRepository
                                            .findByItemIdAndSessionId(item.getId(), sessionId)
                                            .map(CartItem::getCount)
                                            .block();

                                    webTestClient.post()
                                            .uri(uriBuilder -> uriBuilder
                                                    .path(path + "/{id}")
                                                    .queryParam("action", ItemController.CartItemAction.PLUS)
                                                    .build(item.getId()))
                                            .cookie("SESSION", sessionId)
                                            .accept(MediaType.TEXT_HTML)
                                            .exchange()
                                            .expectStatus().isOk();

                                    final Long afterCount = cartItemRepository
                                            .findByItemIdAndSessionId(item.getId(), sessionId)
                                            .map(CartItem::getCount)
                                            .block();

                                    assertThat(afterCount).isEqualTo(beforeCount + 1); // 1 + 1 = 2
                                }
                        ))
                )
                .collectList()
                .block()
                .stream();
    }

    @TestFactory
    public Stream<DynamicTest> decrement_success() throws Exception {
        return itemRepository.findAll()
                .filter(Objects::nonNull)
                .take(10)
                .flatMap(item ->
                        Mono.just(DynamicTest.dynamicTest(
                                "Decrementing item with ID: " + item.getId(),
                                () -> {
                                    itemService.incrementItem(item.getId(), sessionId).block();
                                    itemService.incrementItem(item.getId(), sessionId).block();

                                    final Long beforeCount = cartItemRepository
                                            .findByItemIdAndSessionId(item.getId(), sessionId)
                                            .map(CartItem::getCount)
                                            .block();


                                    webTestClient.post()
                                            .uri(uriBuilder -> uriBuilder
                                                    .path(path + "/{id}")
                                                    .queryParam("action", ItemController.CartItemAction.MINUS)
                                                    .build(item.getId()))
                                            .cookie("SESSION", sessionId)
                                            .accept(MediaType.TEXT_HTML)
                                            .exchange()
                                            .expectStatus().isOk();  // или isFound(), смотрите что возвращает контроллер

                                    final Long afterCount = cartItemRepository
                                            .findByItemIdAndSessionId(item.getId(), sessionId)
                                            .map(CartItem::getCount)
                                            .block();

                                    assertThat(afterCount).isEqualTo(beforeCount - 1);
                                }
                        ))
                )
                .collectList()
                .block()
                .stream();
    }


    @TestFactory
    public Stream<DynamicTest> getOne_success() {
        return itemRepository.findAll()
                .filter(Objects::nonNull)
                .take(10)
                .flatMap(item ->
                        Mono.just(DynamicTest.dynamicTest(
                                "Get item with ID: " + item.getId(),
                                () -> webTestClient.get()
                                        .uri(path + "/" + item.getId())
                                        .cookie("SESSION", sessionId)
                                        .accept(MediaType.TEXT_HTML)
                                        .exchange()
                                        .expectStatus().isOk()
                                        .expectHeader().contentType("text/html")
                                        .expectBody()
                                        .consumeWith(result -> {
                                            final Document doc = Jsoup.parse(new String(result.getResponseBody(), StandardCharsets.UTF_8));

                                            final Element card = doc.select("div.card").first();
                                            assertThat(card).isNotNull();

                                            final String title = card.select("h5.card-title").text();
                                            assertThat(title).isEqualTo(item.getTitle());

                                            final String price = card.select("span.badge.text-bg-success").text();
                                            assertThat(price).contains(item.getPrice().toString());

                                            final String description = card.select("p.card-text").text();
                                            assertThat(description).isEqualTo(item.getDescription());

                                            final Element form = card.select("form[method=post]").first();
                                            assertThat(form).isNotNull();

                                            assertThat(form.attr("action")).isEqualTo("/items/" + item.getId());

                                            final String count = form.select("span").text();
                                            assertThat(count).isEqualTo("0");

                                            assertThat(form.select("button[type=submit][name=action][value=MINUS]")).isNotEmpty();
                                            assertThat(form.select("button[type=submit][name=action][value=PLUS]")).isNotEmpty();

                                            assertThat(form.select("button[type=submit].bi-cart4")).isNotEmpty();
                                        })
                        ))
                )
                .collectList()
                .block()
                .stream();
    }


}











