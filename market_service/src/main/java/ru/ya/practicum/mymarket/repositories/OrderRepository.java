package ru.ya.practicum.mymarket.repositories;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.model.Order;

import java.util.List;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    Flux<Order> findAllByUsername(@NotNull String username);
    Mono<Order> findByIdAndUsername(@NotNull Long id, @NotNull String username);
}
