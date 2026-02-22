package ru.yandex.practicum.mymarket.controllers.dto;

import jakarta.validation.constraints.NotNull;

public record OrderItemDTO(@NotNull Long id,
                           @NotNull String title,
                           @NotNull Long price,
                           @NotNull int count) {

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }


    public Long getPrice() {
        return price;
    }

    public int getCount() {
        return count;
    }
}
