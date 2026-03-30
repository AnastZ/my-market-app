package ru.ya.practicum.mymarket.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table(name = "cart_item")
public class CartItem {

    @Id
    private Long id;
    @NotNull
    private Long cartId;
    @NotNull
    private Long itemId;
    @NotNull
    private String title;
    @NotNull
    private Long oneItemPrice;
    @Min(1)
    private Long count;
    @Version
    private Long version;

    protected CartItem() {
    }

    public CartItem(@NotNull final Long cartId,
                    @NotNull final Long itemId,
                    @NotNull final String title,
                    @NotNull final Long oneItemPrice) {
        this.cartId = cartId;
        this.itemId = itemId;
        this.title = title;
        this.count = 1L;
        this.oneItemPrice = oneItemPrice;
    }

    public CartItem(@NotNull final Long cartId,
                    @NotNull final Long itemId,
                    @NotNull final String title,
                    @NotNull @Min(1) final Long count,
                    @NotNull final Long oneItemPrice) {
        this.cartId = cartId;
        this.itemId = itemId;
        this.title = title;
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

    public String getTitle() {
        return title;
    }

    public Long getVersion() {
        return version;
    }

    public Long getOneItemPrice() {
        return oneItemPrice;
    }

    public Long getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CartItem that = (CartItem) o;
        return count.equals(that.count) && Objects.equals(id, that.id) && Objects.equals(cartId, that.cartId) && Objects.equals(itemId, that.itemId) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cartId, itemId, count, version);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", cartId=" + cartId +
                ", itemId=" + itemId +
                ", title='" + title + '\'' +
                ", oneItemPrice=" + oneItemPrice +
                ", count=" + count +
                ", version=" + version +
                '}';
    }
}
