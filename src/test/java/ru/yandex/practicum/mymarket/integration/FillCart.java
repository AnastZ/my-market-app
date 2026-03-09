package ru.yandex.practicum.mymarket.integration;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.CartRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;

import java.util.Random;
import java.util.function.Supplier;

public interface FillCart extends FillItems{
    default Flux<CartItem> fillDb(final String sessionId,
                                  final CartRepository cartRepository,
                                  final ItemRepository itemRepository,
                                  final CartItemRepository cartItemRepository){
        final Random random = new Random();
        final long min = 1;
        final long max = 100;
        final Supplier<Long> count = ()->random.nextLong((max - min) + 1L) + min;
        return cartRepository.save(new Cart(sessionId))
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
