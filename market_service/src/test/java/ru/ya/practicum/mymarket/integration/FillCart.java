package ru.ya.practicum.mymarket.integration;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.model.Cart;
import ru.ya.practicum.mymarket.model.CartItem;
import ru.ya.practicum.mymarket.repositories.CartItemRepository;
import ru.ya.practicum.mymarket.repositories.CartRepository;
import ru.ya.practicum.mymarket.repositories.ItemRepository;

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
