package ru.yandex.practicum.mymarket.repositories.dao;

import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.Objects;

/**
 * Класс для загрузки данных о количестве товара в корзине.
 */
public class ItemDAO {
    private final Item item;
    private final int count;

    public ItemDAO(@NotNull final Item item, final int count) {
        this.item = item;
        this.count = count;
    }

    public Item getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ItemDAO itemDAO = (ItemDAO) o;
        return Objects.equals(item, itemDAO.item) && Objects.equals(count, itemDAO.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, count);
    }

    @Override
    public String toString() {
        return "ItemDAO{" +
                "item=" + item +
                ", count=" + count +
                '}';
    }

}
