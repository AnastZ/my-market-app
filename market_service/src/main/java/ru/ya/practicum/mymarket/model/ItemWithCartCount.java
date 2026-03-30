package ru.ya.practicum.mymarket.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Objects;

/**
 * Класс для загрузки данных о количестве товара в корзине.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class ItemWithCartCount implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;
    private String description;
    private String imgPath;
    private Long price;
    private int count;

    protected ItemWithCartCount() {

    }

    public ItemWithCartCount(Long id,
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

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ItemWithCartCount itemWithCartCount = (ItemWithCartCount) o;
        return count == itemWithCartCount.count && Objects.equals(id, itemWithCartCount.id) && Objects.equals(title, itemWithCartCount.title) && Objects.equals(description, itemWithCartCount.description) && Objects.equals(imgPath, itemWithCartCount.imgPath) && Objects.equals(price, itemWithCartCount.price);
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
