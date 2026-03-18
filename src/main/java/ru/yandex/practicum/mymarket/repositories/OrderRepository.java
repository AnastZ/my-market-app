package ru.yandex.practicum.mymarket.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;

import java.util.List;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

}
