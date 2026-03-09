package ru.yandex.practicum.mymarket.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;

import java.util.Objects;


public class OrderItem {
    @Id
    private Long id;
    @NotNull
    private Long orderId;
    @NotNull
    private Long itemId;
    @NotNull
    private String title;
    @NotNull
    @Column("PRICE_AT_ORDER")
    private Long price;
    @Min(1)
    private int count;
    @Version
    private Long version;

    protected OrderItem() {
    }

    public OrderItem(@NotNull final Long orderId,
                     @NotNull final CartItem item) {
        this.orderId = orderId;
        this.itemId = item.getItemId();
        this.title = item.getTitle();
        this.price = item.getOneItemPrice();
        this.count = Math.toIntExact(item.getCount());
    }

    public Long getId() {
        return id;
    }

    public Long getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public Long getPrice() {
        return price;
    }

    public int getCount() {
        return count;
    }

    public void setOrderId(Long orderId) {
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
