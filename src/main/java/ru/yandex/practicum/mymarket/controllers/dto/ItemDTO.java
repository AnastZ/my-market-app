package ru.yandex.practicum.mymarket.controllers.dto;

import jakarta.validation.constraints.NotNull;

public record ItemDTO (@NotNull Long id,
                       @NotNull String title,
                       @NotNull String description,
                       @NotNull String imgPath,
                       @NotNull Long price,
                       int count){
    public static ItemDTO ofSpecial(){
        return new ItemDTO(-1L, "", "", "", 0L, 0);
    }

    public Long getId() {
        return id;
    }


    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImgPath() {
        return imgPath;
    }


    public Long getPrice() {
        return price;
    }

    public int getCount() {
        return count;
    }
}
