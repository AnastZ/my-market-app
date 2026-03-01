package ru.yandex.practicum.mymarket.model;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Table(name = "order_table")
public class Order {
    @Id
    @ReadOnlyProperty
    private Long id;

    @Transient
    private List<OrderItem> orderItems;
    @Version
    private Long version;
    public Order() {
        orderItems = new ArrayList<>();
    }

    public void addOrderItem(@NotNull final OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrderId(this);
    }

    public void removeOrderItem(@NotNull final OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrderId(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderDate=" +
                '}';
    }
}
