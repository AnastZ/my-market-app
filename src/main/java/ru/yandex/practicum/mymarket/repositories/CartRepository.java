package ru.yandex.practicum.mymarket.repositories;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Cart;

import java.util.Optional;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, Long> {

    Mono<Cart> findBySessionId(@NotNull String sessionId);
}
