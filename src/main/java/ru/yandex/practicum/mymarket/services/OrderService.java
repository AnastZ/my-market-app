package ru.yandex.practicum.mymarket.services;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import javassist.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.controllers.dto.DTOConvertor;
import ru.yandex.practicum.mymarket.controllers.dto.OrderDTO;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repositories.OrderItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderRepository;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DTOConvertor<Order, OrderDTO> orderDTOConvertor;
    private final CartItemService cartItemService;

    public OrderService(@NotNull final OrderRepository orderRepository,
                        @NotNull final OrderItemRepository orderItemRepository,
                        @NotNull final DTOConvertor<Order, OrderDTO> orderDTOConvertor,
                        @NotNull final CartItemService cartItemService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderDTOConvertor = orderDTOConvertor;
        this.cartItemService = cartItemService;
    }

    private static <T> Mono<T> notFound(final Long id) {
        return Mono.error(new NotFoundException("Заказ не найден: " + id));
    }

    private static <T> Mono<T> notFound() {
        return Mono.error(new NotFoundException("Пустой результат поиска."));
    }

    /**
     * Загразить для заказа товары, которые связаны с ним.
     *
     * @param order заказ.
     * @return источник данных.
     */
    private Mono<Order> fillOrder(final Order order) {
        return orderItemRepository.findAllByOrderId(order.getId())
                .switchIfEmpty(notFound())
                .collectList()
                .map(items -> {
                    order.setOrderItems(items);
                    return order;
                });
    }

    /**
     * Получить все заказы.
     *
     * @return источник данных с заказами.
     */
    @Transactional(readOnly = true)
    public @NotNull Flux<OrderDTO> findAll() {
        return orderRepository.findAll()
                .switchIfEmpty(notFound())
                .flatMap(this::fillOrder)
                .map(orderDTOConvertor::toDTO);
    }

    /**
     * Найти заказ по уникальному номеру.
     *
     * @param id уникальный номер заказа.
     * @return источник данных с найденным заказом, если заказ не найден генерируется ошибка NotFoundException.
     */
    @Transactional(readOnly = true)
    public @NotNull Mono<OrderDTO> findById(@NotNull final Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(notFound(id))
                .flatMap(this::fillOrder)
                .map(orderDTOConvertor::toDTO);
    }

    /**
     * Сохранить заказ. Из БД извлекаются объекты, помещённые в корзину,
     * и создаётся заказ, напомленный ими.
     *
     * @param sessionId уникальный номер сессии.
     * @return источник данных с сохранённым заказом.
     */
    @Transactional
    public @NotNull Mono<OrderDTO> save(@NotNull @NotBlank final String sessionId) {
        return cartItemService.findItemsBySessionId(sessionId)
                .collectList()
                .switchIfEmpty(notFound())
                .flatMap(items -> orderRepository.save(new Order())
                        .flatMap(order -> {
                            final List<OrderItem> orderItems = items.stream()
                                    .map(item -> new OrderItem(order.getId(), item))
                                    .toList();
                            return orderItemRepository.saveAll(orderItems)
                                    .collectList()
                                    .map(savedItems -> {
                                        order.setOrderItems(savedItems);
                                        return order;
                                    });
                        }))
                .map(orderDTOConvertor::toDTO);
    }
}
