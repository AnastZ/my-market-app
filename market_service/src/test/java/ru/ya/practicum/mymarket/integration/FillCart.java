package ru.ya.practicum.mymarket.integration;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.model.Cart;
import ru.ya.practicum.mymarket.model.CartItem;
import ru.ya.practicum.mymarket.model.Item;
import ru.ya.practicum.mymarket.repositories.CartItemRepository;
import ru.ya.practicum.mymarket.repositories.CartRepository;
import ru.ya.practicum.mymarket.repositories.ItemRepository;

import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface FillCart{

    static Stream<Long> itemIds() {
        return Stream.iterate(1L, n -> n + 1L).limit(21);
    }

    default Flux<Item> fillItems(final ItemRepository itemRepository){
        return Flux.fromIterable(itemIds().toList())
                .map(itemId -> new Item("title" + itemId, "desc" + itemId, "path", itemId))
                .collectList()
                .flatMapMany(itemRepository::saveAll);
    }

    default Flux<CartItem> fillDb(final String username,
                                  final CartRepository cartRepository,
                                  final ItemRepository itemRepository,
                                  final CartItemRepository cartItemRepository){
        final Random random = new Random();
        final long min = 1;
        final long max = 100;
        final Supplier<Long> count = ()->random.nextLong((max - min) + 1L) + min;
        return cartRepository.save(new Cart(username))
                .flatMapMany(savedCart ->
                        fillItems(itemRepository)
                                .map(savedItem -> {
                                    return new CartItem(savedCart.getId(), savedItem.getId(), savedItem.getTitle(), count.get(), savedItem.getPrice());
                                })
                                .collectList() // Собираем все CartItem в список
                                .flatMapMany(cartItemRepository::saveAll) // Сохраняем все элементы корзины в БД
                );
    }
}
