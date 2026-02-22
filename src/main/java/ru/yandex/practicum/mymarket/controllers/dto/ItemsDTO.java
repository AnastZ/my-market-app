package ru.yandex.practicum.mymarket.controllers.dto;

import ru.yandex.practicum.mymarket.model.Paging;

import java.util.List;

public record ItemsDTO(List<List<ItemDTO>> items, Paging paging) {

}
