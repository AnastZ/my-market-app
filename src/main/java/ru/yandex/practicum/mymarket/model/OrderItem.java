package ru.yandex.practicum.mymarket.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    private Long price;

    private int count;

    protected OrderItem() {
    }

    public OrderItem(@NotNull final Item item,
                     @NotNull final Long price,
                     int count) {
        this.item = item;
        this.price = price;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public Long getPrice() {
        return price;
    }

    public int getCount() {
        return count;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return count == orderItem.count && Objects.equals(id, orderItem.id) && Objects.equals(item, orderItem.item) && Objects.equals(price, orderItem.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, item, price, count);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", item=" + item +
                ", price=" + price +
                ", count=" + count +
                '}';
    }
}
