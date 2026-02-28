package ru.yandex.practicum.mymarket.integration;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OrderControllerTest extends AbstractController implements FillItems {

    private final String path = "/orders";
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ItemRepository itemRepository;

    private void generateOrders(final List<Item> items) {
        final List<Order> orders = new ArrayList<>();
        final Random random = new Random();
        final int min = 1;
        final int max = 100;
        final Supplier<Integer> count = () -> {
            return random.nextInt((max - min) + 1) + min;
        };
        final Supplier<Integer> item = () -> {
            return random.nextInt((items.size() - 1) + 1);
        };

        Stream.generate(Order::new).limit(21)
                .forEach(order -> {
                    orders.add(order);
                    final Item it = items.get(item.get());
                    order.addOrderItem(new OrderItem(it, it.getPrice(), count.get()));
                });
        orderRepository.saveAll(orders);
    }

    public void fillDb() {
        final List<Item> items = FillItems.super.fillItems(itemRepository);
        generateOrders(items);
    }

    @Test
    public void getAll_success() throws Exception {
        fillDb();
        mockMvc.perform(get(path)
                        .contentType(MediaType.TEXT_HTML)
                        .accept(MediaType.TEXT_HTML)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attribute("orders", hasItem(allOf(
                        hasProperty("id"),
                        hasProperty("items", hasItem(allOf(
                                hasProperty("id"),
                                hasProperty("title"),
                                hasProperty("price", is(not(0))),
                                hasProperty("count", is(not(0)))
                        ))),
                        hasProperty("totalSum", is(not(0))))
                )));
    }
    @ParameterizedTest
    @ValueSource(longs = {1, 2})
    public void getOne_success(final Long orderId) throws Exception {
        fillDb();
        mockMvc.perform(get(path + "/" + orderId)
                        .contentType(MediaType.TEXT_HTML)
                        .accept(MediaType.TEXT_HTML)
                        .param("orderId", "" + orderId)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("order", allOf(
                        hasProperty("id"),
                        hasProperty("items", hasItem(allOf(
                                hasProperty("id"),
                                hasProperty("title"),
                                hasProperty("price", is(not(0))),
                                hasProperty("count", is(not(0)))
                        ))),
                        hasProperty("totalSum", is(not(0))))
                ));
    }
}
