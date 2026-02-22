package ru.yandex.practicum.mymarket.controllers.dto;

public record ItemDTO (Long id, String title, String description, String imgPath, Long price, int count){
    public static ItemDTO ofSpecial(){
        return new ItemDTO(-1L, "", "", "", 0L, 0);
    }

}
