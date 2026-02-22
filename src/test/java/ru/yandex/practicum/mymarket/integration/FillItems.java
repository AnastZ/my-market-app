package ru.yandex.practicum.mymarket.integration;

import org.springframework.mock.web.MockHttpSession;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public interface FillItems {

    static Stream<Long> itemIds() {
        return Stream.iterate(1L, n -> n + 1L).limit(21);
    }

    default List<Item> fillItems(final ItemRepository itemRepository){
        final List<Item> items = new ArrayList<>();
        itemIds().forEach(itemId -> {
            items.add(new Item("title" + itemId, "desc" + itemId, "path", itemId));
        });
        return itemRepository.saveAll(items);
    }
}
