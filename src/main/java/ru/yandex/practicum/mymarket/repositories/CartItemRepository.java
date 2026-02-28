package ru.yandex.practicum.mymarket.repositories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.repositories.dao.ItemDAO;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {

    @Query("SELECT ct FROM CartItem ct WHERE ct.item.id = :item AND ct.cart.sessionId = :session")
    Mono<CartItem> findByItemIdAndSessionId(@Param("item") Long itemId,
                                                @Param("session") String sessionId);

    @Query("SELECT ct FROM CartItem ct WHERE ct.cart.sessionId = :session")
    Flux<CartItem> getCartItems(@NotNull @NotBlank @Param("session") final String sessionId);

}
