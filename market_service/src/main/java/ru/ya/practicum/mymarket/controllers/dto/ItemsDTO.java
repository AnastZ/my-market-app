package ru.ya.practicum.mymarket.controllers.dto;

import jakarta.validation.constraints.NotNull;
import ru.ya.practicum.mymarket.model.Paging;

import java.util.List;

public record ItemsDTO(@NotNull List<List<ItemDTO>> items, @NotNull Paging paging) {

}
