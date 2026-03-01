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

    private Long orderId;

    private Long itemId;

    private Long price;

    private int count;
    @Version
    private Long version;

    protected OrderItem() {
    }

    public OrderItem(@NotNull final Long orderId,
                     @NotNull final CartItem item) {
        this.orderId = orderId;
        this.itemId = item.getItemId();
        this.price = item.getOneItemPrice();
        this.count = item.getCount();
    }

    public Long getId() {
        return id;
    }

    public Item getItemId() {
        return itemId;
    }

    public Long getPrice() {
        return price;
    }

    public int getCount() {
        return count;
    }

    public void setOrderId(Order orderId) {
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return count == orderItem.count && Objects.equals(id, orderItem.id) && Objects.equals(itemId, orderItem.itemId) && Objects.equals(price, orderItem.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, itemId, price, count);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", item=" + itemId +
                ", price=" + price +
                ", count=" + count +
                '}';
    }
}
