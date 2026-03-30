package ru.ya.practicum.mymarket.controllers.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ya.practicum.mymarket.model.Order;
import ru.ya.practicum.mymarket.model.OrderItem;
import ru.ya.practicum.mymarket.model.ItemWithCartCount;

import java.util.List;

@Configuration
public class ConverterConfig {
    @Bean
    public EntityConvertor<ItemWithCartCount, ItemDTO> itemDTOConvertor() {
        return i -> {
            return new ItemDTO(i.getId(), i.getTitle(), i.getDescription(), i.getImgPath(), i.getPrice(), i.getCount());
        };
    }

    @Bean
    public EntityConvertor<OrderItem, OrderItemDTO> orderItemDTOConvertor() {
        return it -> new OrderItemDTO(it.getItemId(), it.getTitle(), it.getPrice(), it.getCount());
    }

    @Bean
    public EntityConvertor<Order, OrderDTO> orderDTOConvertor(@NotNull final EntityConvertor<OrderItem, OrderItemDTO> orderItemConverter) {
        return order -> {
            final List<OrderItemDTO> items = order.getOrderItems()
                    .stream()
                    .map(orderItemConverter::convert)
                    .toList();
            final Long totalSum = items.stream()
                    .map(OrderItemDTO::price)
                    .reduce(Long::sum)
                    .orElse(0L);
            return new OrderDTO(order.getId(), items, totalSum);
        };
    }
}
