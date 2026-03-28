package ru.ya.practicum.mymarket.services;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.controllers.dto.EntityConvertor;
import ru.ya.practicum.mymarket.controllers.dto.OrderDTO;
import ru.ya.practicum.mymarket.model.NotFoundException;
import ru.ya.practicum.mymarket.model.Order;
import ru.ya.practicum.mymarket.model.OrderItem;
import ru.ya.practicum.mymarket.repositories.OrderItemRepository;
import ru.ya.practicum.mymarket.repositories.OrderRepository;
import ru.ya.practicum.payment.client.api.BalanceApi;

import javax.naming.ServiceUnavailableException;
import java.util.List;

@Service
public class OrderService {

    private final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EntityConvertor<Order, OrderDTO> orderEntityConvertor;
    private final CartItemService cartItemService;
    private final BalanceApi balanceApi;
    private final PaymentServiceHealthChecker paymentServiceHealthChecker;

    public OrderService(@NotNull final OrderRepository orderRepository,
                        @NotNull final OrderItemRepository orderItemRepository,
                        @NotNull final EntityConvertor<Order, OrderDTO> orderEntityConvertor,
                        @NotNull final CartItemService cartItemService,
                        @NotNull final BalanceApi balanceApi,
                        @NotNull final PaymentServiceHealthChecker paymentServiceHealthChecker) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderEntityConvertor = orderEntityConvertor;
        this.cartItemService = cartItemService;
        this.balanceApi = balanceApi;
        this.paymentServiceHealthChecker = paymentServiceHealthChecker;
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
                .map(orderEntityConvertor::convert);
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
                .map(orderEntityConvertor::convert);
    }

    /**
     * Сохранить заказ. Из БД извлекаются объекты, помещённые в корзину,
     * и создаётся заказ, наполненный ими.
     *
     * @param sessionId уникальный номер сессии.
     * @return источник данных с сохранённым заказом.
     */
    @Transactional
    public @NotNull Mono<OrderDTO> createOrder(@NotNull @NotBlank final String sessionId) {
        return paymentServiceHealthChecker.isHealthy()
                .flatMap(healthy -> {
                    if (!healthy)
                        return Mono.error(new ServiceUnavailableException("Payment service is not available."));

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
                            .flatMap(order -> {
                                final OrderDTO orderDTO = orderEntityConvertor.convert(order);

                                return balanceApi.payment(sessionId, orderDTO.getTotalSum())
                                        .flatMap(balance -> {
                                            if (balance >= 0) {
                                                return Mono.just(orderDTO);
                                            } else {
                                                return Mono.error(new PaymentError(
                                                        String.format("Insufficient funds. Balance: %d, Required: %d",
                                                                balance, orderDTO.getTotalSum())));
                                            }
                                        })
                                        .onErrorResume(e -> Mono.error(new PaymentError(
                                                "Payment failed: " + e.getMessage())));
                            });
                }).onErrorResume(ServiceUnavailableException.class, e -> {
                    log.error("Payment service unavailable: {}", e.getMessage());
                    return Mono.error(e);
                })
                .onErrorResume(PaymentError.class, e -> {
                    log.error("Payment failed for session {}: {}", sessionId, e.getMessage());
                    return Mono.error(e);
                });
    }
}
