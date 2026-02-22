package ru.yandex.practicum.mymarket.integration;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.repositories.dao.ItemDAO;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerTest {

    private final String path = "/items";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Value("${item.list-size}")
    private int itemListSize;
    @Autowired
    private CartItemRepository cartItemRepository;

    @BeforeEach
    public void setup() {
        final List<Item> items = new ArrayList<>();
        for (long i = 1; i <= 21; i++) {
            items.add(new Item("title" + i, "desc" + i, "path", i));
        }
        itemRepository.saveAll(items);
    }

    @AfterEach
    public void teardown() {
        itemRepository.deleteAll();
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
                .andDo(print())
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
                .andDo(print())
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
                final List<ItemDTO> current = modelList.get(i).stream().filter(it->it.id()!=-1L).toList();
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
                .andDo(print())
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
}











