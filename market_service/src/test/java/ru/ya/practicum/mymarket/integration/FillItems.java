package ru.ya.practicum.mymarket.integration;

import org.springframework.mock.web.MockHttpSession;
import reactor.core.publisher.Flux;
import ru.ya.practicum.mymarket.model.Item;
import ru.ya.practicum.mymarket.repositories.ItemRepository;

import java.util.stream.Stream;

public interface FillItems {

    static Stream<Long> itemIds() {
        return Stream.iterate(1L, n -> n + 1L).limit(21);
    }

    default Flux<Item> fillItems(final ItemRepository itemRepository){
        return Flux.fromIterable(itemIds().toList())
                .map(itemId -> new Item("title" + itemId, "desc" + itemId, "path", itemId))
                .collectList()
                .flatMapMany(itemRepository::saveAll);
    }
}
