package ru.ya.practicum.mymarket.services;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.controllers.ItemController;
import ru.ya.practicum.mymarket.controllers.dto.DTOConvertor;
import ru.ya.practicum.mymarket.controllers.dto.ItemDTO;
import ru.ya.practicum.mymarket.model.CartItem;
import ru.ya.practicum.mymarket.controllers.dto.ItemsDTO;
import ru.ya.practicum.mymarket.model.NotFoundException;
import ru.ya.practicum.mymarket.model.Paging;
import ru.ya.practicum.mymarket.repositories.ItemRepository;
import ru.ya.practicum.mymarket.repositories.dao.ItemDAO;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

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

        return items.collectList()
                .flatMapMany(originalList -> {
                    final int listSize = originalList.size();
                    final int fullGroups = listSize / nestedSize;
                    final int remainder = listSize % nestedSize;

                    final List<List<ItemDTO>> result = new ArrayList<>();

                    for (int i = 0; i < fullGroups; i++) {
                        final int start = i * nestedSize;
                        result.add(new ArrayList<>(
                                originalList.subList(start, start + nestedSize)
                        ));
                    }
                    if (remainder > 0) {
                        final List<ItemDTO> lastGroup = new ArrayList<>(
                                originalList.subList(fullGroups * nestedSize, listSize)
                        );
                        while (lastGroup.size() < nestedSize) {
                            lastGroup.add(ItemDTO.ofSpecial());
                        }
                        result.add(lastGroup);
                    }
                    return Flux.fromIterable(result);
                })
                .collectList();
    }

    /**
     * Получить страницу объектов из БД с сортировкой.
     *
     * @param search     поисковой запрос (фильтрация по названию/описанию), если без фильтрации, то следует передать пустую строку, значение null неприемлимо.
     * @param sortMethod метод сортировки.
     * @return список объектов.
     */
    private @NotNull Flux<ItemDAO> getAll(@NotNull final String search,
                                          @NotNull final ItemController.SortMethod sortMethod,
                                          @NotNull @NotBlank final String sessionId) {

        final Sort sort = switch (sortMethod) {
            case ALPHA -> Sort.by("title");
            case PRICE -> Sort.by("price");
            default -> Sort.unsorted();
        };
        return itemRepository
                .findAllInCart(search, sessionId, sort);

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
        final int skip = (pageNumber - 1) * pageSize;
        return getAll(search, sortMethod, sessionId)
                .switchIfEmpty(Mono.error(new NotFoundException("Товары не найдены.")))
                .collectList()
                .flatMapMany(allItems -> {
                    final int totalItems = allItems.size();
                    final int totalPages = (int) Math.ceil((double) totalItems / pageSize);

                    if (pageNumber > totalPages && totalPages > 0) {
                        return Flux.error(new NotFoundException("Страница не найдена."));
                    }

                    final Paging paging = new Paging(
                            pageNumber,
                            pageSize,
                            pageNumber > 1,
                            pageNumber < totalPages
                    );
                    return Flux.fromIterable(allItems)
                            .skip(skip)
                            .take(pageSize)
                            .map(itemDTOConvertor::toDTO)
                            .collectList()
                            .flatMapMany(paginatedList ->
                                    convertToNestedLists(Flux.fromIterable(paginatedList), listSize)
                                            .map(nested -> new ItemsDTO(nested, paging))
                            );
                })
                .next();
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
                .flatMap(item -> {
                    item.incrementCount();
                    return cartItemService.save(item);
                })
                .switchIfEmpty(
                        itemRepository.findById(itemId)
                                .switchIfEmpty(notFound(itemId))
                                .flatMap(item ->
                                        cartService.getOrCreateBySessionId(sessionId)
                                                .switchIfEmpty(Mono.error(new NotFoundException("Корзина не найдена.")))
                                                .flatMap(c -> {
                                                    final CartItem newCartItem = new CartItem(
                                                            c.getId(),
                                                            item.getId(),
                                                            item.getTitle(),
                                                            item.getPrice()
                                                    );
                                                    return Mono.defer(() -> cartItemService.save(newCartItem));
                                                })
                                )
                )
                .then();
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
                .then();
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














