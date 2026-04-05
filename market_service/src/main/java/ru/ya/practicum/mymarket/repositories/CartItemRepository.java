package ru.ya.practicum.mymarket.repositories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.model.CartItem;

@Repository
public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {

    @Query("""
            SELECT * FROM Cart_Item ct 
            LEFT JOIN cart c ON c.id = ct.cart_id
            WHERE ct.item_id = :item AND c.username = :username
            """)
    Mono<CartItem> findByItemIdAndUsername(@Param("item") Long itemId,
                                           @Param("username") String username);

    @Query("""
            SELECT * FROM Cart_Item ct 
            LEFT JOIN cart c ON c.id = ct.cart_id
            WHERE c.username = :username
            """)
    Flux<CartItem> getCartItems(@NotNull @NotBlank @Param("username") final String username);

}
