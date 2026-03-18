package ru.yandex.practicum.mymarket.controllers.dto;

import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.mymarket.model.Paging;

import java.util.List;

public record ItemsDTO(@NotNull List<List<ItemDTO>> items, @NotNull Paging paging) {

}
