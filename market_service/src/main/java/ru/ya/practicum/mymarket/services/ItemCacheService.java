package ru.ya.practicum.mymarket.services;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.controllers.ItemController;
import ru.ya.practicum.mymarket.model.SortMethod;
import ru.ya.practicum.mymarket.repositories.ItemRepository;
import ru.ya.practicum.mymarket.model.ItemWithCartCount;

import java.time.Duration;


@Service
public class ItemCacheService {
    private final Logger log = LoggerFactory.getLogger(ItemCacheService.class);
    private static final String CACHE_ITEMS_LIST = "itemsList";
    private static final String CACHE_ITEMS_CART = "itemsCart";
    private static final String CACHE_ITEM = "item";
    private static final Duration duration = Duration.ofMinutes(10);
    private final ItemRepository itemRepository;

    public ItemCacheService(@NotNull final ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public static String getCacheKey(final String search,
                                     final String sort,
                                     final String sessionId) {
        return search + ":" + sort + ":" + sessionId;
    }
    /**
     * Получение списка элементов согласно поисковому запросу и методу сортировки.
     * Товары берутся из кэша, либо загружаются из БД (при этом кэшируюясь).
     *
     * @param search     поисковой запрос.
     * @param sortMethod метод сортировки.
     * @param sessionId  уникальный номер сессии.
     * @return
     */
    @Cacheable(value = CACHE_ITEMS_LIST, key = "#search + ':' + #sortMethod.name() + ':' + #sessionId")
    public Flux<ItemWithCartCount> getAllCached(@NotNull final String search,
                                                @NotNull final SortMethod sortMethod,
                                                @NotNull final String sessionId) {

        return loadAllAndCache(search, sortMethod, sessionId);
    }

    /**
     * Получить товары в корзине из кэша, либо из БД.
     *
     * @param sessionId идентификатор сессии (корзины).
     * @return
     */
    @Cacheable(value = CACHE_ITEMS_CART, key = "#sessionId")
    public Flux<ItemWithCartCount> getAllCached(@NotNull final String sessionId) {

        return loadAllAndCache(sessionId);
    }

    /**
     * Получить товар по уникальному номеру из кэша, либо из БД.
     *
     * @param itemId    уникальный номер товара.
     * @param sessionId уникальный номер сессии.
     * @return
     */
    @Cacheable(value = CACHE_ITEM, key = "#itemId + ':' + #sessionId")
    public Mono<ItemWithCartCount> getById(@NotNull final Long itemId, @NotNull final String sessionId) {
        return loadOneAndCache(itemId, sessionId);
    }

    /**
     * Загрузить все товары из БД и кэшировать.
     *
     * @param search     поисковой запрос.
     * @param sortMethod метод сортировки.
     * @param sessionId  уникальный номер сессии.
     * @return
     */
    private Flux<ItemWithCartCount> loadAllAndCache(@NotNull final String search,
                                                    @NotNull final SortMethod sortMethod,
                                                    @NotNull final String sessionId) {
        final Sort sort = switch (sortMethod) {
            case ALPHA -> Sort.by("title");
            case PRICE -> Sort.by("price");
            default -> Sort.unsorted();
        };
        return itemRepository.findAllWithCart(search, sessionId, sort);
    }

    /**
     * Загрузить все товары корзины (по уникальному значению сессии).
     *
     * @param sessionId уникальный значению сессии.
     * @return
     */
    private Flux<ItemWithCartCount> loadAllAndCache(@NotNull final String sessionId) {

        return itemRepository.findAllInCart(sessionId);
    }

    /**
     * Загрузить один товар по его уникальному номеру.
     *
     * @param itemId    уникальный номер товара.
     * @param sessionId уникальный номер сессии.
     * @return
     */
    private Mono<ItemWithCartCount> loadOneAndCache(@NotNull final Long itemId,
                                                    @NotNull final String sessionId) {
        return itemRepository.findByIdAndSessionId(itemId, sessionId);
    }

    /**
     * Инвалидировать весь кэш товаров.
     */
    @CacheEvict(value = {CACHE_ITEMS_LIST, CACHE_ITEMS_CART, CACHE_ITEM}, allEntries = true)
    public Mono<Void> evictItemsCache() {
        return Mono.empty();
    }

    /**
     * Инвалидировать кэш конкретного товара.
     */
    @CacheEvict(value = CACHE_ITEM, key = "#itemId + ':' + #sessionId")
    public Mono<Void> evictItemCache(@NotNull final Long itemId, @NotNull final String sessionId) {
        return Mono.empty();
    }

    /**
     * Обновить кэш товара после обновления в БД.
     */
    @CachePut(value = CACHE_ITEM, key = "#itemId + ':' + #sessionId")
    public Mono<ItemWithCartCount> updateItemCache(@NotNull final Long itemId,
                                                   @NotNull final String sessionId,
                                                   @NotNull final ItemWithCartCount updatedItem) {
        return Mono.just(updatedItem);
    }
}
