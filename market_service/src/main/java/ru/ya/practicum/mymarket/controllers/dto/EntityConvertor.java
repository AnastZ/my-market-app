package ru.ya.practicum.mymarket.controllers.dto;


public interface EntityConvertor<T, DTO>{
    DTO convert(T entity);
}
