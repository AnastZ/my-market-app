package ru.yandex.practicum.mymarket.controllers.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderDTO(@NotNull Long id,
                       @NotNull List<OrderItemDTO> items,
                       @NotNull Long totalSum) {

    public Long getId() {
        return id;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public Long getTotalSum() {
        return totalSum;
    }
}
