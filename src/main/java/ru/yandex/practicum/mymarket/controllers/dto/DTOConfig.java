package ru.yandex.practicum.mymarket.controllers.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repositories.dao.ItemDAO;

import java.util.List;

@Configuration
public class DTOConfig {
    @Bean
    public DTOConvertor<ItemDAO, ItemDTO> itemDTOConvertor() {
        return item -> {
            final Item i = item.getItem();
            return new ItemDTO(i.getId(), i.getTitle(), i.getDescription(), i.getImgPath(), i.getPrice(), item.getCount());
        };
    }

    @Bean
    public DTOConvertor<OrderItem, OrderItemDTO> orderItemDTOConvertor() {
        return it -> {
            return new OrderItemDTO(it.getItem().getId(), it.getItem().getTitle(), it.getPrice(), it.getCount());
        };
    }

    @Bean
    public DTOConvertor<Order, OrderDTO> orderDTOConvertor(@NotNull final DTOConvertor<OrderItem, OrderItemDTO> orderItemConverter) {
        return order -> {
            final List<OrderItemDTO> items = order.getOrderItems()
                    .stream()
                    .map(orderItemConverter::toDTO)
                    .toList();
            final Long totalSum = items.stream()
                    .map(OrderItemDTO::price)
                    .reduce(Long::sum)
                    .orElse(0L);
            return new OrderDTO(order.getId(), items, totalSum);
        };
    }

    @Bean
    public DTOConvertor<CartItem, OrderItem> getCartItemDTOConvertor() {
        return ci -> {
            return new OrderItem(ci.getItem(), ci.getItem().getPrice(), ci.getCount());
        };
    }
}
