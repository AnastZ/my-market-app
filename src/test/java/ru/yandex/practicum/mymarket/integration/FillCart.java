package ru.yandex.practicum.mymarket.integration;

import org.springframework.mock.web.MockHttpSession;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.CartRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public interface FillCart extends FillItems{
    default void fillDb(final MockHttpSession session,
                        final CartRepository cartRepository,
                        final ItemRepository itemRepository,
                        final CartItemRepository cartItemRepository){
        final Cart cart = cartRepository.save(new Cart(session.getId()));
        final List<Item> items = FillItems.super.fillItems(itemRepository);
        final List<CartItem> cartItems = new ArrayList<CartItem>();
        final Random random = new Random();
        final int min = 1;
        final int max = 100;
        for(final Item item : items){
            final CartItem ct = new CartItem(cart, item);
            cartItems.add(ct);
            ct.setCount(random.nextInt((max - min) + 1) + min);
        }
        cartItemRepository.saveAll(cartItems);
    }
}
