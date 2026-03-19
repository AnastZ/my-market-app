package ru.ya.practicum.mymarket.controllers.dto;


public interface DTOConvertor <T, DTO>{

    DTO toDTO(T entity);
}
