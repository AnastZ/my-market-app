package ru.ya.practicum.mymarket.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.ya.practicum.mymarket.model.Order;
@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

}
