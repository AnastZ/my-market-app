package ru.yandex.practicum.mymarket.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table("cart_item")
public class CartItem {

    @Id
    @ReadOnlyProperty
    private Long id;
    @NotNull
    private Long cartId;
    @NotNull
    private Long itemId;
    @NotNull
    private Long oneItemPrice;
    @Min(1)
    private int count;
    @Version
    private Long version;

    protected CartItem() {}

    public CartItem(@NotNull final Long cartId,
                    @NotNull final Long itemId,
                    @NotNull final Long oneItemPrice) {
        this.cartId = cartId;
        this.itemId = itemId;
        this.count = 1;
        this.oneItemPrice = oneItemPrice;
    }
    public CartItem(@NotNull final Long cartId,
                    @NotNull final Long itemId,
                    @Min(1) final int count,
                    @NotNull final Long oneItemPrice) {
        this.cartId = cartId;
        this.itemId = itemId;
        this.count = count;
        this.oneItemPrice = oneItemPrice;
    }
    public void incrementCount() {
        this.count++;
    }

    public void decrementCount() {
        this.count--;
    }

    public Long getId() {
        return id;
    }

    public Long getCartId() {
        return cartId;
    }

    public Long getItemId() {
        return itemId;
    }

    public Long getVersion() {
        return version;
    }

    public Long getOneItemPrice() {
        return oneItemPrice;
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CartItem that = (CartItem) o;
        return count == that.count && Objects.equals(id, that.id) && Objects.equals(cartId, that.cartId) && Objects.equals(itemId, that.itemId) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cartId, itemId, count, version);
    }

    @Override
    public String toString() {
        return "CartItemDAO{" +
                "id=" + id +
                ", cartId=" + cartId +
                ", itemId=" + itemId +
                ", count=" + count +
                ", version=" + version +
                '}';
    }
}
