package ru.yandex.practicum.mymarket.model;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;

import java.util.Objects;


public class OrderItem {
    @Id
    @ReadOnlyProperty
    private Long id;

    private Order order;

    private Item item;

    private Long price;

    private int count;
    @Version
    private Long version;

    protected OrderItem() {
    }

    public OrderItem(@NotNull final Order order,
                     @NotNull final CartItem item) {
        this.order = order;
        this.item = item.getItem();
        this.price = item.getItem().getPrice();
        this.count = item.getCount();
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
