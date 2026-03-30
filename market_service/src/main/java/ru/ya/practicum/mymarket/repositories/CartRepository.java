package ru.ya.practicum.mymarket.repositories;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.model.Cart;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, Long> {

    Mono<Cart> findBySessionId(@NotNull String sessionId);
}
