package ru.yandex.practicum.mymarket.services;

import jakarta.persistence.NoResultException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.controllers.dto.DTOConvertor;
import ru.yandex.practicum.mymarket.controllers.dto.OrderDTO;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderRepository;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final DTOConvertor<Order, OrderDTO> orderDTOConvertor;
    private final CartItemService cartItemService;
    private final DTOConvertor<CartItem, OrderItem> cartItemDTOConvertor;

    public OrderService(@NotNull final OrderRepository orderRepository,
                        @NotNull final DTOConvertor<Order, OrderDTO> orderDTOConvertor,
                        @NotNull final CartItemService cartItemService,
                        @NotNull final DTOConvertor<CartItem, OrderItem> cartItemDTOConvertor) {
        this.orderRepository = orderRepository;
        this.orderDTOConvertor = orderDTOConvertor;
        this.cartItemService = cartItemService;
        this.cartItemDTOConvertor = cartItemDTOConvertor;
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> findAll() {
        final List<Order> orders = orderRepository.findAll();
        return orders.stream().map(orderDTOConvertor::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public OrderDTO findById(@NotNull final Long id) throws NoResultException {
        final Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            throw new NoResultException("Order with id " + id + " does not exist.");
        }
        return orderDTOConvertor.toDTO(order.get());
    }

    @Transactional
    public OrderDTO save(@NotNull @NotBlank final String sessionId) {
        final List<CartItem> itemsInCart = cartItemService.findItemsBySessionId(sessionId);
        final Order order = new Order();
        itemsInCart.stream()
                .map(cartItemDTOConvertor::toDTO)
                .forEach(order::addOrderItem);
        orderRepository.save(order);
        return orderDTOConvertor.toDTO(order);
    }
}
