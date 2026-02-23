package ru.yandex.practicum.mymarket.unit;

import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.mymarket.controllers.dto.DTOConfig;
import ru.yandex.practicum.mymarket.controllers.dto.DTOConvertor;
import ru.yandex.practicum.mymarket.controllers.dto.ItemDTO;
import ru.yandex.practicum.mymarket.integration.FillItems;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.services.CartItemService;
import ru.yandex.practicum.mymarket.services.CartService;
import ru.yandex.practicum.mymarket.services.ItemService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {ItemService.class})
@Import(DTOConfig.class)
public class ItemServiceTest implements FillItems {

    @Autowired
    private ItemService itemService;

    @MockitoBean
    private ItemRepository itemRepository;
    @MockitoBean
    private CartService cartService;
    @MockitoBean
    private CartItemService cartItemService;


    private DTOConvertor<Item, ItemDTO> itemDTOConvertor = new DTOConvertor<>() {
        @Override
        public @NotNull ItemDTO toDTO(@NotNull final Item item) {
            return new ItemDTO(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(), item.getPrice(), 1);
        }
    };

    @Test
    public void convertList_success() {
        final List<ItemDTO> items = FillItems.super.fillItems(itemRepository).stream().map(itemDTOConvertor::toDTO).toList();
        final int nestedSize = 5;
        assertTrue(itemService.convertToNestedLists(items, nestedSize)
                .stream()
                .allMatch(list->list.size() == nestedSize));

    }
}
