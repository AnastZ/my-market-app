package ru.yandex.practicum.mymarket.integration;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import ru.yandex.practicum.mymarket.controllers.ItemController;
import ru.yandex.practicum.mymarket.controllers.dto.ItemDTO;
import ru.yandex.practicum.mymarket.controllers.dto.ItemsDTO;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.repositories.dao.ItemDAO;
import ru.yandex.practicum.mymarket.services.ItemService;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ItemControllerTest extends AbstractController implements FillItems{

    private final String path = "/items";

    @Autowired
    private ItemRepository itemRepository;

    @Value("${item.list-size}")
    private int itemListSize;

    @BeforeEach
    public void setup() {
        FillItems.super.fillItems(itemRepository);
    }

    @Test
    public void findAll_withoutParams() throws Exception {
        final int defNumber = 1;
        final int defSize = 5;
        final MockHttpSession session = new MockHttpSession();
        final int countPages = itemRepository.findAll("", session.getId(), PageRequest.of(defNumber, defSize)).getTotalPages();
        mockMvc.perform(get(path)
                        .session(session)
                        .contentType(MediaType.TEXT_HTML)
                        .accept(MediaType.TEXT_HTML)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("search"))
                .andExpect(model().attributeExists("sort"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attribute("items", Matchers.not(empty())))
                .andExpect(model().attribute("items", hasItem(hasSize(itemListSize))))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("sort", ItemController.SortMethod.NO))
                .andExpect(model().attribute("paging", allOf(
                                hasProperty("pageNumber", is(defNumber)),
                                hasProperty("pageSize", is(defSize)),
                                hasProperty("hasPrevious", is(defNumber != 1)),
                                hasProperty("hasNext", is(defNumber < countPages))
                        )
                ));
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 2", "3, 4"})
    public void findAll_withoutSearchAndSort(final int pageNumber, final int pageSize) throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final int countPages = itemRepository.findAll("", session.getId(), PageRequest.of(pageNumber, pageSize)).getTotalPages();
        mockMvc.perform(get(path)
                        .session(session)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize))
                        .contentType(MediaType.TEXT_HTML)
                        .accept(MediaType.TEXT_HTML)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("search"))
                .andExpect(model().attributeExists("sort"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attribute("items", Matchers.not(empty())))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("sort", ItemController.SortMethod.NO))
                .andExpect(model().attribute("paging", allOf(
                                hasProperty("pageNumber", is(pageNumber)),
                                hasProperty("pageSize", is(pageSize)),
                                hasProperty("hasPrevious", is(pageNumber != 1)),
                                hasProperty("hasNext", is(pageNumber < countPages))
                        )
                ));
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 2, NO", "3, 4, ALPHA", "2, 5, PRICE"})
    public void findAll_withSort(final int pageNumber,
                                 final int pageSize,
                                 final ItemController.SortMethod method) throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final Page<ItemDAO> itemsFromDB = itemRepository.findAll("", session.getId(), PageRequest.of(pageNumber, pageSize));
        final int countPages = itemsFromDB.getTotalPages();

        final Map<String, Object> model = mockMvc.perform(get(path)
                        .session(session)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("sort", method.toString())
                        .contentType(MediaType.TEXT_HTML)
                        .accept(MediaType.TEXT_HTML)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("search"))
                .andExpect(model().attributeExists("sort"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attribute("items", Matchers.not(empty())))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("sort", method))
                .andExpect(model().attribute("paging", allOf(
                                hasProperty("pageNumber", is(pageNumber)),
                                hasProperty("pageSize", is(pageSize)),
                                hasProperty("hasPrevious", is(pageNumber != 1)),
                                hasProperty("hasNext", is(pageNumber < countPages))
                        )
                ))
                .andReturn().getModelAndView().getModel();
        final List<List<ItemDTO>> modelList = ((List<List<ItemDTO>>) model.get("items"));

        final Object sortModel = model.get("sort");
        final Comparator<ItemDTO> currentComparator = sortModel.equals(ItemController.SortMethod.PRICE) ?
                Comparator.comparing(ItemDTO::price)
                : sortModel.equals(ItemController.SortMethod.ALPHA) ?
                Comparator.comparing(ItemDTO::title)
                : null;
        if (Objects.nonNull(currentComparator)) {
            for (int i = 0; i < modelList.size(); i++) {
                final List<ItemDTO> current = modelList.get(i).stream().filter(it -> it.id() != -1L).toList();
                assertThat(current)
                        .isSortedAccordingTo(currentComparator);
                if (i + 1 < modelList.size()) {
                    List<ItemDTO> next = modelList.get(i + 1);
                    ItemDTO lastOfNext = next.get(next.size() - 1);
                    ItemDTO firstOfCurrent = current.get(0);
                    int cmp = currentComparator.compare(lastOfNext, firstOfCurrent);
                    assertThat(cmp)
                            .isLessThan(0);
                }
            }
        }
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 2, tit", "3, 4, ''"})
    public void findAll_withSearch(final int pageNumber, final int pageSize, final String search) throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final int countPages = itemRepository.findAll("", session.getId(), PageRequest.of(pageNumber, pageSize)).getTotalPages();
        mockMvc.perform(get(path)
                        .session(session)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("search", search)
                        .contentType(MediaType.TEXT_HTML)
                        .accept(MediaType.TEXT_HTML)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("search"))
                .andExpect(model().attributeExists("sort"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attribute("items", Matchers.not(empty())))
                .andExpect(model().attribute("search", search))
                .andExpect(model().attribute("sort", ItemController.SortMethod.NO))
                .andExpect(model().attribute("paging", allOf(
                                hasProperty("pageNumber", is(pageNumber)),
                                hasProperty("pageSize", is(pageSize)),
                                hasProperty("hasPrevious", is(pageNumber != 1)),
                                hasProperty("hasNext", is(pageNumber < countPages))
                        )
                ));
    }


    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemService itemService;

    @TestFactory
    public Stream<DynamicTest> increment_success() throws Exception {
        return itemRepository.findAll().stream()
                .filter(Objects::nonNull) // Убедитесь, что item не null
                .limit(10)
                .map(item -> DynamicTest.dynamicTest("Incrementing item with ID: " + item.getId(), () -> {
                    final MockHttpSession session = new MockHttpSession();
                    itemService.incrementItem(item.getId(), session.getId());
                    final Optional<CartItem> beforeItem = cartItemRepository.findByItemIdAndSessionId(item.getId(), session.getId());
                    if (beforeItem.isEmpty())
                        return;
                    mockMvc.perform(post(path)
                                    .session(session)
                                    .param("id", item.getId().toString())
                                    .param("action", "PLUS")
                                    .contentType(MediaType.TEXT_HTML)
                                    .accept(MediaType.TEXT_HTML)
                                    .characterEncoding("utf-8"))
                            .andExpect(status().isFound());
                    assertTrue(beforeItem.get().getCount() < cartItemRepository.findByItemIdAndSessionId(item.getId(), session.getId()).get().getCount());

                }));
    }

    @TestFactory
    public Stream<DynamicTest> decrement_success() throws Exception {
        return itemRepository.findAll().stream()
                .filter(Objects::nonNull)
                .limit(10)
                .map(item -> DynamicTest.dynamicTest("Decrementing item with ID: " + item.getId(), () -> {
                    final Long itemId = item.getId();
                    final MockHttpSession session = new MockHttpSession();
                    itemService.incrementItem(itemId, session.getId());
                    final Optional<CartItem> beforeItem = cartItemRepository.findByItemIdAndSessionId(itemId, session.getId());

                    if (beforeItem.isEmpty()) {
                        throw new IllegalStateException("Failed to prepare CartItem for test with itemId: " + itemId);
                    }

                    mockMvc.perform(post(path)
                                    .session(session)
                                    .param("id", itemId.toString())
                                    .param("action", "MINUS")
                                    .contentType(MediaType.TEXT_HTML)
                                    .accept(MediaType.TEXT_HTML)
                                    .characterEncoding("utf-8"))
                            .andExpect(status().isFound());

                    final Optional<CartItem> afterItem = cartItemRepository.findByItemIdAndSessionId(itemId, session.getId());

                    if (afterItem.isEmpty()) {
                        assertTrue(beforeItem.get().getCount() > 0, "Expected count to be greater than 0 before decrementing to 0.");
                        return;
                    }

                    assertTrue(beforeItem.get().getCount() > afterItem.get().getCount(),
                            "Expected cart item count to decrease for itemId: " + itemId);

                }));
    }

    @TestFactory
    public Stream<DynamicTest> incrementFromOne_success() throws Exception {
        return itemRepository.findAll().stream()
                .filter(item -> item != null)
                .limit(10)
                .map(item -> DynamicTest.dynamicTest("Incrementing item with ID: " + item.getId(), () -> {
                    final Long itemId = item.getId();
                    final Item existingItem = item;
                    final MockHttpSession session = new MockHttpSession();

                    itemService.incrementItem(itemId, session.getId());

                    final Optional<CartItem> beforeItemOpt = cartItemRepository.findByItemIdAndSessionId(itemId, session.getId());
                    if (beforeItemOpt.isEmpty()) {
                        throw new IllegalStateException("Failed to create initial CartItem for itemId: " + itemId);
                    }
                    final CartItem beforeCartItem = beforeItemOpt.get();
                    final int initialCount = beforeCartItem.getCount();

                    mockMvc.perform(post(path + "/" + itemId)
                                    .session(session)
                                    .param("id", itemId.toString())
                                    .param("action", "PLUS")
                                    .contentType(MediaType.TEXT_HTML)
                                    .accept(MediaType.TEXT_HTML)
                                    .characterEncoding("utf-8"))
                            .andExpect(status().isOk())
                            .andExpect(model().attributeExists("item"))
                            .andExpect(model().attribute("item", allOf(
                                            hasProperty("id", is(itemId)),
                                            hasProperty("title", is(existingItem.getTitle())),
                                            hasProperty("description", is(existingItem.getDescription())),
                                            hasProperty("imgPath", is(existingItem.getImgPath())),
                                            hasProperty("price", is(existingItem.getPrice())),
                                            hasProperty("count", is(initialCount + 1))
                                    )
                            ));

                    final Optional<CartItem> afterItemOpt = cartItemRepository.findByItemIdAndSessionId(itemId, session.getId());
                    if (afterItemOpt.isEmpty()) {
                        throw new IllegalStateException("CartItem for itemId " + itemId + " unexpectedly disappeared after increment.");
                    }
                    final CartItem afterCartItem = afterItemOpt.get();

                    assertTrue(afterCartItem.getCount() == initialCount + 1,
                            "Expected cart item count to increment from " + initialCount + " to " + (initialCount + 1) + " for itemId: " + itemId);

                }));
    }

    @ParameterizedTest
    @MethodSource("itemIds")
    public void getOne_success(final Long itemId) throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final Item item = itemRepository.findById(itemId).orElse(null);
        if (Objects.isNull(item))
            return;
        mockMvc.perform(get(path + "/" + itemId)
                        .session(session)
                        .param("id", itemId.toString())
                        .contentType(MediaType.TEXT_HTML)
                        .accept(MediaType.TEXT_HTML)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("item"))
                .andExpect(model().attribute("item", allOf(
                                hasProperty("id", is(itemId)),
                                hasProperty("title", is(item.getTitle())),
                                hasProperty("description", is(item.getDescription())),
                                hasProperty("imgPath", is(item.getImgPath())),
                                hasProperty("price", is(item.getPrice())),
                                hasProperty("count", is(0))
                        )
                ));
    }
}











