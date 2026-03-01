package ru.yandex.practicum.mymarket.repositories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.*;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repositories.dao.ItemDAO;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long>{

    @Query("SELECT new ru.yandex.practicum.mymarket.repositories.dao.ItemDAO(item, COALESCE(ci.count, 0L)) " +
            " FROM Item item " +
            " LEFT JOIN CartItem ci ON ci.item = item" +
            " WHERE (LOWER(item.title) LIKE LOWER(CONCAT('%', :search, '%'))" +
            " OR LOWER(item.description) LIKE LOWER(CONCAT('%', :search, '%'))) ")
    Flux<ItemDAO> findAllInCart(@NotNull String search,
                                @NotNull @NotBlank String sessionId,
                                @NotNull Sort sort);

    @Query("SELECT new ru.yandex.practicum.mymarket.repositories.dao.ItemDAO(item, COALESCE(ci.count, 0L)) " +
            " FROM Item item " +
            " LEFT JOIN CartItem ci ON ci.item = item " +
            " WHERE ci.cart.sessionId = :session")
    Flux<ItemDAO> findAllInCart(@NotNull @NotBlank @Param("session") String sessionId);


    @Query("SELECT new ru.yandex.practicum.mymarket.repositories.dao.ItemDAO(item, COALESCE(ci.count, 0L)) " +
            " FROM Item item " +
            " LEFT JOIN CartItem ci ON ci.item.id = item.id AND ci.cart.sessionId = :session " +
            " WHERE item.id = :item")
    Mono<ItemDAO> findByIdAndSessionId(@NotNull @Param("item") Long itemId,
                                       @NotNull @NotBlank @Param("session") String sessionId);


    Mono<Item> findById(@NotNull Long id);
}
