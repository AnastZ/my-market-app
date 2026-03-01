package ru.yandex.practicum.mymarket.services;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.*;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.controllers.ItemController;
import ru.yandex.practicum.mymarket.controllers.dto.DTOConvertor;
import ru.yandex.practicum.mymarket.controllers.dto.ItemDTO;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.controllers.dto.ItemsDTO;
import ru.yandex.practicum.mymarket.model.Paging;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.repositories.dao.ItemDAO;

import java.util.ArrayList;
import java.util.List;

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

    private static <T> Mono<T> notFound(final Long id) {
        return Mono.error(new NotFoundException("Товар не найден: " + id));
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
    public @NotNull Mono<List<List<ItemDTO>>> convertToNestedLists(@NotNull @NotEmpty final Flux<ItemDTO> items,
                                                                   @Min(1) final int nestedSize) {

        return items.buffer(nestedSize)
                .map(list -> {
                    if (list.size() < nestedSize) {
                        final List<ItemDTO> paddedList = new ArrayList<>(list);
                        while (paddedList.size() < nestedSize) {
                            paddedList.add(ItemDTO.ofSpecial());
                        }
                        return paddedList;
                    }
                    return list;
                }).collectList();
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
    private @NotNull Flux<ItemDAO> getAll(@Min(1) final int pageNumber,
                                          @Min(1) final int pageSize,
                                          @NotNull final String search,
                                          @NotNull final ItemController.SortMethod sortMethod,
                                          @NotNull @NotBlank final String sessionId) {

        final int skip = (pageNumber - 1) * pageSize;
        final Sort sort = switch (sortMethod) {
            case ALPHA -> Sort.by("title");
            case PRICE -> Sort.by("price");
            default -> Sort.unsorted();
        };
        return itemRepository
                .findAllInCart(search, sessionId, sort)
                .skip(skip)
                .take(pageSize);

    }

    /**
     * Получить объекты из БД.
     *
     * @param pageNumber номер страницы.
     * @param pageSize   количество объектов на странице.
     * @param search     поисковой запрос (фильтрация по названию/описанию), если фильтрация не нужна, то передать пустую строку.
     * @param sortMethod метод сортировки.
     * @return найденные объекты.
     */
    @Transactional(readOnly = true)
    public @NotNull Mono<ItemsDTO> getItems(@Min(1) final int pageNumber,
                                            @Min(1) final int pageSize,
                                            @NotNull final String search,
                                            @NotNull final ItemController.SortMethod sortMethod,
                                            @NotNull @NotBlank final String sessionId) {
        return getAll(pageNumber, pageSize, search, sortMethod, sessionId)
                .switchIfEmpty(Mono.error(new NotFoundException("Товары не найдены.")))
                .map(itemDTOConvertor::toDTO)
                .collectList()
                .flatMap(itemDAOList -> {
                    final Paging paging = new Paging(pageNumber, pageSize, pageNumber != 1, itemDAOList.size() < pageSize);
                    return convertToNestedLists(Flux.fromIterable(itemDAOList), listSize)
                            .map(nested -> new ItemsDTO(nested, paging));
                });
    }

    /**
     * Увеличить количество товара в корзине.
     *
     * @param itemId    уникальный номер товара.
     * @param sessionId уникальный номер сессии.
     * @return пустой источник данных.
     */
    @Transactional
    public Mono<Void> incrementItem(@NotNull final Long itemId,
                                    @NotNull @NotBlank final String sessionId) {

        return cartItemService.findByIdInCart(itemId, sessionId)
                .switchIfEmpty(itemRepository.findById(itemId)
                        .switchIfEmpty(notFound(itemId))
                        .flatMap(item -> cartService.getOrCreateBySessionId(sessionId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Корзина не найдена.")))
                                .flatMap(c -> cartItemService.save(new CartItem(c.getId(), item.getId(), item.getPrice())))))
                .flatMap(item -> {
                    item.incrementCount();
                    return cartItemService.save(item);
                })
                .flatMap(t -> Mono.empty());
    }

    /**
     * Уменьшить количество товара в корзине на единицу.
     * Если количество товара стало равным 0, то он удаляется из корзины.
     *
     * @param itemId    уникальный номер товара.
     * @param sessionId уникальный номер сессии.
     */
    @Transactional
    public Mono<Void> decrementItem(@NotNull final Long itemId,
                                    @NotNull @NotBlank final String sessionId) {

        return cartItemService.findByIdInCart(itemId, sessionId)
                .switchIfEmpty(notFound(itemId))
                .flatMap(c -> {
                    c.decrementCount();
                    if (c.getCount() == 0) {
                        return cartItemService.delete(c);
                    }
                    return cartItemService.save(c);
                })
                .flatMap(i -> Mono.empty());
    }

    /**
     * Найти объект в БД по id.
     * Id сессии нужен, чтобы создать объект ItemDAO, который хранит количество объекта в корзине для переданной сессии.
     *
     * @param itemId    уникальный номер товара.
     * @param sessionId уникальный номер сессии.
     * @return товар.
     */
    public Mono<ItemDTO> findItemInCart(@NotNull final Long itemId,
                                        @NotNull @NotBlank final String sessionId) {

        return itemRepository.findByIdAndSessionId(itemId, sessionId)
                .switchIfEmpty(notFound(itemId))
                .map(itemDTOConvertor::toDTO);
    }

    /**
     * Получить все объекты в корзине.
     *
     * @param sessionId уникальный номер сессии.
     * @return объекты в корзине.
     */
    public Flux<ItemDTO> findAllInCart(@NotNull final String sessionId) {
        return itemRepository.findAllInCart(sessionId)
                .map(itemDTOConvertor::toDTO);
    }
}














