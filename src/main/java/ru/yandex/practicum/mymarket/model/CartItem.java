package ru.yandex.practicum.mymarket.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@Entity
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Cart cart;
    @ManyToOne(fetch = FetchType.EAGER)
    private Item item;
    private int count;

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
