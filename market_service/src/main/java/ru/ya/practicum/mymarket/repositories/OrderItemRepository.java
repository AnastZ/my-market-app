package ru.ya.practicum.mymarket.repositories;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.ya.practicum.mymarket.model.OrderItem;
@Repository
public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {

    Flux<OrderItem> findAllByOrderId(@NotNull Long orderId);
}
