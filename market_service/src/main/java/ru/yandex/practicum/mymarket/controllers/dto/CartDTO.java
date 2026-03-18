package ru.yandex.practicum.mymarket.controllers.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CartDTO(@NotNull List<ItemDTO> items,
                      @NotNull @Min(0L) Long total) {
}
