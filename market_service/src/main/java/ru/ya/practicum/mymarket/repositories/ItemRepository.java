package ru.ya.practicum.mymarket.repositories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.*;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.model.Item;
import ru.ya.practicum.mymarket.repositories.dao.ItemDAO;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {

    @Query("""
                SELECT i.id, i.title, i.description, i.img_path as imgPath, i.price,
                COALESCE((
                    SELECT ci.count
                    FROM cart_item ci
                    JOIN cart c ON ci.cart_id = c.id
                    WHERE ci.item_id = i.id AND c.session_id = :sessionId
                    LIMIT 1), 0) as count
                FROM item i
                WHERE (LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Flux<ItemDAO> findAllWithCart(@NotNull String search,
                                  @NotNull @NotBlank String sessionId,
                                  @NotNull Sort sort);

    @Query("""
            SELECT i.id, i.title, i.description, i.img_path as imgPath, i.price, 
                   COALESCE((
                    SELECT ci.count
                    FROM cart_item ci
                    JOIN cart c ON ci.cart_id = c.id
                    WHERE ci.item_id = i.id AND c.session_id = :sessionId
                    LIMIT 1), 0) as count
            FROM item i
            LEFT JOIN cart_item ci ON ci.item_id = i.id 
            LEFT JOIN cart c ON ci.cart_id = c.id AND c.session_id = :sessionId
            """)
    Flux<ItemDAO> findAllInCart(@NotNull @NotBlank @Param("sessionId") String sessionId);


    @Query("""
            SELECT i.id, i.title, i.description, i.img_path as imgPath, i.price,\s
                                             COALESCE((
                                              SELECT ci.count
                                              FROM cart_item ci
                                              JOIN cart c ON ci.cart_id = c.id
                                              WHERE ci.item_id = i.id AND c.session_id = :session
                                              LIMIT 1), 0) as count
                                      FROM item i
                                      WHERE i.id = :item
            """)
    Mono<ItemDAO> findByIdAndSessionId(@NotNull @Param("item") Long itemId,
                                       @NotNull @NotBlank @Param("session") String sessionId);


    Mono<Item> findById(@NotNull Long id);
}
