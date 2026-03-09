package ru.yandex.practicum.mymarket.repositories.dao;

import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.Objects;

/**
 * Класс для загрузки данных о количестве товара в корзине.
 */
public class ItemDAO {
    private Long id;

    private String title;
    private String description;
    private String imgPath;
    private Long price;
    private final int count;

    public ItemDAO(Long id,
                   String title,
                   String description,
                   String imgPath,
                   Long price,
                   int count) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imgPath = imgPath;
        this.price = price;
        this.count = count;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ItemDAO itemDAO = (ItemDAO) o;
        return count == itemDAO.count && Objects.equals(id, itemDAO.id) && Objects.equals(title, itemDAO.title) && Objects.equals(description, itemDAO.description) && Objects.equals(imgPath, itemDAO.imgPath) && Objects.equals(price, itemDAO.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, imgPath, price, count);
    }

    @Override
    public String toString() {
        return "ItemDAO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", imgPath='" + imgPath + '\'' +
                ", price=" + price +
                ", count=" + count +
                '}';
    }
}
