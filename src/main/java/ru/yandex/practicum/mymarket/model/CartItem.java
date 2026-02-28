package ru.yandex.practicum.mymarket.model;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;

import java.util.Objects;

public class CartItem {
    @Id
    @ReadOnlyProperty
    private Long id;

    private Cart cart;
    private Item item;
    private int count;
    @Version
    private Long version;
    protected CartItem() {
    }

    /**
     * Добавление товара в корзину. Количество товаров = 1.
     *
     * @param cart корзина, привязанная к id сессии.
     * @param item товар.
     */
    public CartItem(@NotNull final Cart cart,
                    @NotNull final Item item) {
        this.cart = cart;
        this.item = item;
        this.count = 1;
    }

    public void incrementCount() {
        this.count++;
    }

    public void decrementCount() {
        this.count--;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return count == cartItem.count && Objects.equals(id, cartItem.id) && Objects.equals(cart, cartItem.cart) && Objects.equals(item, cartItem.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cart, item);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", cart=" + cart +
                ", item=" + item +
                ", count=" + count +
                '}';
    }
}
