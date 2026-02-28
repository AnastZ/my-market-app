package ru.yandex.practicum.mymarket.services;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.NoResultException;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.controllers.ItemController;
import ru.yandex.practicum.mymarket.controllers.dto.DTOConvertor;
import ru.yandex.practicum.mymarket.controllers.dto.ItemDTO;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.controllers.dto.ItemsDTO;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Paging;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.repositories.dao.ItemDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ItemRepository itemRepository;
    private final CartService cartService;
    private final CartItemService cartItemService;

    private final int listSize;
    private final DTOConvertor<ItemDAO, ItemDTO> itemDTOConvertor;

    public ItemService(@NotNull final ItemRepository itemRepository,
                       @NotNull final CartService cartService,
                       @NotNull final CartItemService cartItemService,
                       @Value("${item.list-size}") @NotNull final int listSize,
                       @NotNull final DTOConvertor<ItemDAO, ItemDTO> itemDTOConvertor) {
        this.itemRepository = itemRepository;
        this.cartService = cartService;
        this.cartItemService = cartItemService;
        this.listSize = listSize;
        this.itemDTOConvertor = itemDTOConvertor;
    }

    @PostConstruct
    public void init() {
        cartService.setItemService(this);
    }

    /**
     * Разделить список объектов на множество списков с одинаковой длиной,
     * если не хватает объектов для заполнения последнего списка,
     * то добавить объекты-заглушки (пустой объект Item с id = -1L).
     *
     * @param items      список объектов.
     * @param nestedSize размер вложенного списка.
     * @return список из списков.
     */
    public @NotNull List<List<ItemDTO>> convertToNestedLists(@NotNull @NotEmpty final List<ItemDTO> items,
                                                             @Min(1) final int nestedSize) {
        final List<ItemDTO> modifyItems = new ArrayList<>(items);
        final int shortage = nestedSize - (items.size() % nestedSize);
        for (int i = 0; i < shortage; i++) {
            modifyItems.add(ItemDTO.ofSpecial());
        }
        final List<List<ItemDTO>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (final ItemDTO item : modifyItems) {
            if (result.getLast().size() >= nestedSize) {
                result.add(new ArrayList<>());
            }
            result.getLast().add(item);
        }
        return result;
    }

    /**
     * Получить страницу объектов из БД с сортировкой.
     *
     * @param pageNumber номер страницы.
     * @param pageSize   количество объектов на странице.
     * @param search     поисковой запрос (фильтрация по названию/описанию), если без фильтрации, то следует передать пустую строку, значение null неприемлимо.
     * @param sortMethod метод сортировки.
     * @return список объектов.
     */
    private @NotNull Page<ItemDAO> getAll(@Min(1) final int pageNumber,
                                          @Min(1) final int pageSize,
                                          @NotNull final String search,
                                          @NotNull final ItemController.SortMethod sortMethod,
                                          @NotNull @NotBlank final String sessionId) {
        switch (sortMethod) {
            case ALPHA -> {
                return itemRepository.findAll(search, sessionId, PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "title")));
            }
            case PRICE -> {
                return itemRepository.findAll(search, sessionId, PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "price")));
            }
            default -> {
                return itemRepository.findAll(search, sessionId, PageRequest.of(pageNumber, pageSize));
            }
        }
    }

    /**
     * Получить объекты из БД.
     *
     * @param pageNumber номер страницы.
     * @param pageSize   количество объектов на странице.
     * @param search     поисковой запрос (фильтрация по названию/описанию), если фильтрация не нужна, то передать пустую строку.
     * @param sortMethod метод сортировки.
     * @return найденные объекты.
     * @throws NoResultException если объекты не найдены.
     */
    @Transactional(readOnly = true)
    public @NotNull ItemsDTO getItems(@Min(1) final int pageNumber,
                                      @Min(1) final int pageSize,
                                      @NotNull final String search,
                                      @NotNull final ItemController.SortMethod sortMethod,
                                      @NotNull @NotBlank final String sessionId) throws NoResultException {
        final Page<ItemDAO> items = getAll(pageNumber, pageSize, search, sortMethod, sessionId);
        if (items.isEmpty()) {
            throw new NoResultException("No items found.");
        }

        return new ItemsDTO(convertToNestedLists(items.getContent().stream().map(itemDTOConvertor::toDTO).toList(), listSize),
                new Paging(pageNumber, pageSize, pageNumber != 1, !items.isLast()));
    }

    @Transactional
    public void incrementItem(@NotNull final Long itemId,
                              @NotNull @NotBlank final String sessionId) throws NoResultException {
        final Optional<CartItem> existCartItem = cartItemService.findById(itemId, sessionId);
        if (existCartItem.isPresent()) {
            final CartItem cartItem = existCartItem.get();
            cartItem.incrementCount();
            cartItemService.save(cartItem);
            return;
        }
        final Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) {
            throw new NoResultException("No item found.");
        }
        final Cart cart = cartService.getOrCreateBySessionId(sessionId);
        final CartItem cartItem = new CartItem(cart, item.get());
        cartItemService.save(cartItem);
    }

    /**
     * Уменьшить количество товара в корзине на единицу.
     * Если количество товара стало равным 0, то он удаляется из корзины.
     *
     * @param itemId    уникальный номер товара.
     * @param sessionId уникальный номер сессии.
     */
    @Transactional
    public void decrementItem(@NotNull final Long itemId,
                              @NotNull @NotBlank final String sessionId) {
        final Optional<CartItem> existCartItem = cartItemService.findById(itemId, sessionId);
        if (existCartItem.isEmpty()) {
            logger.warn("В корзине нет объекта, чтобы уменьшить его количество в ней.");
            return;
        }
        final CartItem cartItem = existCartItem.get();
        cartItem.decrementCount();
        if (cartItem.getCount() == 0) {
            cartItemService.delete(cartItem);
            return;
        }
        cartItemService.save(cartItem);
    }

    /**
     * Найти объект в БД по id.
     * Id сессии нужен, чтобы создать объект ItemDAO, который хранит количество объекта в корзине для переданной сессии.
     *
     * @param itemId    уникальный номер товара.
     * @param sessionId уникальный номер сессии.
     * @return товар.
     * @throws NoResultException если товар не найден в БД.
     */
    public ItemDTO findItem(@NotNull final Long itemId,
                            @NotNull @NotBlank final String sessionId) throws NoResultException {

        final Optional<ItemDAO> item = itemRepository.findByIdAndSessionId(itemId, sessionId);
        if (item.isEmpty()) {
            throw new NoResultException("No item found.");
        }
        return itemDTOConvertor.toDTO(item.get());
    }

    /**
     * Получить все объекты в корзине.
     *
     * @param sessionId уникальный номер сессии.
     * @return объекты в корзине.
     */
    public List<ItemDTO> findAll(@NotNull final String sessionId) {
        return itemRepository.findAll(sessionId, Pageable.unpaged()).getContent().stream().map(itemDTOConvertor::toDTO).toList();
    }
}














