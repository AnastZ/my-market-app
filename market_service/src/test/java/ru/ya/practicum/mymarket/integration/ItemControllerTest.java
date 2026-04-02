package ru.ya.practicum.mymarket.integration;

import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.controllers.ItemController;
import ru.ya.practicum.mymarket.model.CartItem;
import ru.ya.practicum.mymarket.model.Item;
import ru.ya.practicum.mymarket.model.SortMethod;
import ru.ya.practicum.mymarket.repositories.CartItemRepository;
import ru.ya.practicum.mymarket.repositories.CartRepository;
import ru.ya.practicum.mymarket.repositories.ItemRepository;
import ru.ya.practicum.mymarket.services.ItemService;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Import(SecurityConfiguration.class)
@ActiveProfiles("test")
public class ItemControllerTest extends AbstractTestWithRedis implements FillCart {

    static final String path = "/items";

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ItemService itemService;

    @MockitoBean
    private ReactiveJwtDecoder jwtDecoder;

    @Value("${item.list-size}")
    private int itemListSize;

    @Override
    void setUp() {
        super.setUp();
        FillCart.super.fillDb(username, cartRepository, itemRepository, cartItemRepository);
    }

    @Test
    @WithAnonymousUser
    public void findAll_withoutParams_anonymous() {
        final int defNumber = 1;
        final int defSize = 5;
        final int countPages = (int) Math.ceil((double) getCountItems() / defSize);
        webTestClient.get()
                .uri(path)
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html")
                .expectBody()
                .consumeWith(result -> {
                    final Document doc = Jsoup.parse(new String(result.getResponseBody(), StandardCharsets.UTF_8));
                    checkItems(doc, defSize, defNumber, countPages, SortMethod.NO);
                });
    }

    @Test
    @WithMockUser(username = username)
    public void findAll_withoutParams_authenticated() {
        final int defNumber = 1;
        final int defSize = 5;
        final int countPages = (int) Math.ceil((double) getCountItems() / defSize);
        webTestClient.get()
                .uri(path)
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html")
                .expectBody()
                .consumeWith(result -> {
                    final Document doc = Jsoup.parse(new String(result.getResponseBody(), StandardCharsets.UTF_8));
                    checkItems(doc, defSize, defNumber, countPages, SortMethod.NO);
                });
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 5", "2, 10"})
    @WithAnonymousUser
    public void findAll_withoutSearchAndSort(final int pageNumber, final int pageSize) {
        final int countPages = (int) Math.ceil((double) getCountItems() / pageSize);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("pageNumber", pageNumber)
                        .queryParam("pageSize", pageSize)
                        .build())
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html")
                .expectBody()
                .consumeWith(result -> {

                    Document doc = Jsoup.parse(new String(result.getResponseBody(), StandardCharsets.UTF_8));
                    checkItems(doc, pageSize, pageNumber, countPages, SortMethod.NO);
                });
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 5, NO", "2, 10, ALPHA", "2, 5, PRICE"})
    @WithAnonymousUser
    public void findAll_withSort(final int pageNumber,
                                 final int pageSize,
                                 final SortMethod method) {

        final int countPages = (int) Math.ceil((double) getCountItems() / pageSize);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("pageNumber", pageNumber)
                        .queryParam("pageSize", pageSize)
                        .queryParam("sort", method.toString())
                        .build())
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html")
                .expectBody()
                .consumeWith(result -> {
                    final Document doc = Jsoup.parse(new String(result.getResponseBody(), StandardCharsets.UTF_8));
                    checkItems(doc, pageSize, pageNumber, countPages, method);
                    checkMenuLinks(true, doc);
                });
    }

    @TestFactory
    @WithMockUser(username = username)
    public Stream<DynamicTest> increment_success() {
        return increment(item->{
            itemService.incrementItem(item.getId(), username).block();

            final Long beforeCount = cartItemRepository
                    .findByItemIdAndUsername(item.getId(), username)
                    .map(CartItem::getCount)
                    .block();

            webTestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(path + "/{id}")
                            .queryParam("action", ItemController.CartItemAction.PLUS)
                            .build(item.getId()))
                    .accept(MediaType.TEXT_HTML)
                    .exchange()
                    .expectStatus().isOk();

            final Long afterCount = cartItemRepository
                    .findByItemIdAndUsername(item.getId(), username)
                    .map(CartItem::getCount)
                    .block();

            assertThat(afterCount).isEqualTo(beforeCount + 1);
        });
    }
    @TestFactory
    @WithAnonymousUser
    public Stream<DynamicTest> increment_error_anonymous() {
        return increment(item-> webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(path + "/{id}")
                        .queryParam("action", ItemController.CartItemAction.PLUS)
                        .build(item.getId()))
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isUnauthorized());
    }
    public Stream<DynamicTest> increment(final Consumer<Item> action) {
        return itemRepository.findAll()
                .filter(Objects::nonNull)
                .take(10)
                .flatMap(item ->
                        Mono.just(DynamicTest.dynamicTest(
                                "Incrementing item with ID: " + item.getId(),
                                () -> action.accept(item)
                        ))
                )
                .collectList()
                .block()
                .stream();
    }
    @TestFactory
    @WithMockUser(username = username)
    public Stream<DynamicTest> decrement_success() throws Exception {
        return itemRepository.findAll()
                .filter(Objects::nonNull)
                .take(10)
                .flatMap(item ->
                        Mono.just(DynamicTest.dynamicTest(
                                "Decrementing item with ID: " + item.getId(),
                                () -> {
                                    itemService.incrementItem(item.getId(), username).block();
                                    itemService.incrementItem(item.getId(), username).block();

                                    final Long beforeCount = cartItemRepository
                                            .findByItemIdAndUsername(item.getId(), username)
                                            .map(CartItem::getCount)
                                            .block();


                                    webTestClient.post()
                                            .uri(uriBuilder -> uriBuilder
                                                    .path(path + "/{id}")
                                                    .queryParam("action", ItemController.CartItemAction.MINUS)
                                                    .build(item.getId()))
                                            .accept(MediaType.TEXT_HTML)
                                            .exchange()
                                            .expectStatus().isOk();  // или isFound(), смотрите что возвращает контроллер

                                    final Long afterCount = cartItemRepository
                                            .findByItemIdAndUsername(item.getId(), username)
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


    public Stream<DynamicTest> getOne_success(BiConsumer<Document, Item> asserts) {
        return itemRepository.findAll()
                .filter(Objects::nonNull)
                .take(10)
                .flatMap(item ->
                        Mono.just(DynamicTest.dynamicTest(
                                "Get item with ID: " + item.getId(),
                                () -> webTestClient.get()
                                        .uri(path + "/" + item.getId())
                                        .accept(MediaType.TEXT_HTML)
                                        .exchange()
                                        .expectStatus().isOk()
                                        .expectHeader().contentType("text/html")
                                        .expectBody()
                                        .consumeWith(result -> {
                                            asserts.accept(Jsoup.parse(new String(result.getResponseBody(), StandardCharsets.UTF_8)), item);
                                        })
                        ))
                )
                .collectList()
                .block()
                .stream();
    }

    @TestFactory
    @WithMockUser
    public Stream<DynamicTest> getOne_success_authenticated() {
        return getOne_success((doc, item) -> {
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

            checkMenuLinks(false, doc);
        });
    }

    @TestFactory
    @WithAnonymousUser
    public Stream<DynamicTest> getOne_success_anonymous() {
        return getOne_success((doc, item) -> checkMenuLinks(true, doc));
    }

    private void checkMenuLinks(final boolean isAnonymous, final Document doc) {
        final Set<Element> set = new HashSet<>();
        set.add(doc.select("a[href=/orders]").first());
        set.add(doc.select("a[href=/cart/items]").first());
        if(isAnonymous) set.add(doc.select("a[href=/logout]").first());
        else set.add(doc.select("a[href=/logout]").first());
        set.add(doc.select("button[value=PLUS]").first());
        set.add(doc.select("button[value=MINUS]").first());
        set.stream()
                .map(Assertions::assertThat)
                .forEach(e->{
                    if(isAnonymous) e.isNull();
                    else e.isNotEmpty();
                });
    }

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

    private void checkSort(final Document doc, final SortMethod sortMethod) {
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
                            final SortMethod method) {
        checkSearch(doc);
        checkSort(doc, method);
        checkPageSize(doc, pageSize);
        checkItemList(doc);
        checkPageNumber(doc, pageNum);
        checkPaging(doc, pageNum, countPages);
    }
}











