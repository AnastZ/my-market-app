package ru.ya.practicum.mymarket.services;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.controllers.ItemController;
import ru.ya.practicum.mymarket.repositories.ItemRepository;
import ru.ya.practicum.mymarket.repositories.dao.ItemDAO;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class ItemCacheService {
    private final Logger log = LoggerFactory.getLogger(ItemCacheService.class);
    private static final String CACHE_KEY_PREFIX_LIST = "items:";
    private static final String CACHE_KEY_PREFIX_ONE = "item:";
    private static final Duration duration = Duration.ofMinutes(10);
    private final ItemRepository itemRepository;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public ItemCacheService(@NotNull final ItemRepository itemRepository,
                            @NotNull final ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.itemRepository = itemRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Получение списка элементов согласно поисковому запросу и методу сортировки.
     * Товары берутся из кэша, либо загружаются из БД (при этом кэшируюясь).
     * @param search поисковой запрос.
     * @param sortMethod метод сортировки.
     * @param sessionId уникальный номер сессии.
     * @return
     */
    public Flux<ItemDAO> getAllCached(@NotNull final String search,
                                      @NotNull final ItemController.SortMethod sortMethod,
                                      @NotNull final String sessionId) {

        return redisTemplate.opsForValue()
                .get(buildCacheKeyForList(search, sortMethod))
                .flatMapMany(value -> Flux.fromIterable(convertToItemDAOList(value)))
                .switchIfEmpty(Flux.defer(() -> loadAllAndCache(search, sortMethod, sessionId)));
    }

    /**
     * Получить товары в корзине из кэша, либо из БД.
     * @param sessionId идентификатор сессии (корзины).
     * @return
     */
    public Flux<ItemDAO> getAllCached(@NotNull final String sessionId) {

        return redisTemplate.opsForValue()
                .get(buildCacheKeyForCart(sessionId))
                .flatMapMany(value -> Flux.fromIterable(convertToItemDAOList(value)))
                .switchIfEmpty(Flux.defer(() -> loadAllAndCache(sessionId)));
    }

    /**
     * Получить товар по уникальному номеру из кэша, либо из БД.
     * @param itemId уникальный номер товара.
     * @param sessionId уникальный номер сессии.
     * @return
     */
    public Mono<ItemDAO> getById(@NotNull final Long itemId, @NotNull final String sessionId) {
        return redisTemplate.opsForValue()
                .get(buildCacheKeyForOne(itemId, sessionId))
                .flatMap(v -> {
                    var opt = convertToItemDAO(v);
                    if (opt.isPresent()) return Mono.just(opt.get());
                    return Mono.empty();
                }).switchIfEmpty(Mono.defer(() -> loadOneAndCache(itemId, sessionId)));
    }

    /**
     * Загрузить все товары из БД и кэшировать.
     * @param search поисковой запрос.
     * @param sortMethod метод сортировки.
     * @param sessionId уникальный номер сессии.
     * @return
     */
    private Flux<ItemDAO> loadAllAndCache(@NotNull final String search,
                                          @NotNull final ItemController.SortMethod sortMethod,
                                          @NotNull final String sessionId) {
        final Sort sort = switch (sortMethod) {
            case ALPHA -> Sort.by("title");
            case PRICE -> Sort.by("price");
            default -> Sort.unsorted();
        };
        return itemRepository.findAllWithCart(search, sessionId, sort)
                .collectList()
                .flatMapMany(items -> {
                    if (items.isEmpty()) return Flux.empty();
                    return redisTemplate.opsForValue().set(buildCacheKeyForList(search, sortMethod), items, duration)
                            .thenReturn(items)
                            .flatMapMany(Flux::fromIterable);
                });
    }

    /**
     * Загрузить все товары корзины (по уникальному значению сессии).
     * @param sessionId уникальный значению сессии.
     * @return
     */
    private Flux<ItemDAO> loadAllAndCache(@NotNull final String sessionId) {

        return itemRepository.findAllInCart(sessionId)
                .collectList()
                .flatMapMany(items -> {
                    if (items.isEmpty()) return Flux.empty();
                    return redisTemplate.opsForValue().set(buildCacheKeyForCart(sessionId), items, duration)
                            .thenReturn(items)
                            .flatMapMany(Flux::fromIterable);

                });
    }

    /**
     * Загрузить один товар по его уникальному номеру.
     * @param itemId уникальный номер товара.
     * @param sessionId уникальный номер сессии.
     * @return
     */
    private Mono<ItemDAO> loadOneAndCache(@NotNull final Long itemId,
                                          @NotNull final String sessionId) {
        return itemRepository.findByIdAndSessionId(itemId, sessionId)
                .flatMap(it -> redisTemplate.opsForValue()
                        .set(buildCacheKeyForOne(itemId, sessionId), it, duration)
                        .thenReturn(it)
                        .flatMap(Mono::just));
    }

    public static String buildCacheKeyForList(@NotNull final String search,
                                              @NotNull final ItemController.SortMethod sortMethod) {
        return String.format("%s%s:%s", CACHE_KEY_PREFIX_LIST, search, sortMethod.name());
    }

    public static String buildCacheKeyForCart(@NotNull final String sessionId) {
        return String.format("%s%s", CACHE_KEY_PREFIX_LIST, sessionId);
    }

    public static String buildCacheKeyForOne(@NotNull final Long itemId,
                                             @NotNull final String sessionId) {
        return String.format("%s%s:%s", CACHE_KEY_PREFIX_ONE, itemId, sessionId);
    }

    /**
     * Инвалидировать весь кэш товаров.
     */
    Mono<Void> evictItemsCache() {
        return redisTemplate.keys("*")
                .flatMap(redisTemplate.opsForValue()::delete)
                .then()
                .doOnSuccess(v -> log.debug("Items cache evicted"));
    }

    /**
     * Конвертировать список, состоящий из объектов, чьи свойства в формате ключ-значение,
     * в список с самими объектами.
     *
     * @param value список.
     * @return список с объектами, либо null, если не удалось конвертировать.
     */
    private @Null List<ItemDAO> convertToItemDAOList(@NotNull final Object value) {
        final List<ItemDAO> result = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (final Object obj : list) {
                if (obj instanceof ItemDAO item) {
                    result.add(item);
                } else if (obj instanceof Map) {
                    final Map<String, Object> map = (Map<String, Object>) obj;
                    ItemDAO item = new ItemDAO(((Number) map.get("id")).longValue(),
                            (String) map.get("title"),
                            (String) map.get("description"),
                            (String) map.get("imgPath"),
                            ((Number) map.get("price")).longValue(),
                            ((Number) map.get("count")).intValue());
                    result.add(item);
                }
            }
        }
        return result.isEmpty() ? null : result;
    }

    private Optional<ItemDAO> convertToItemDAO(@NotNull final Object value) {
        if (value instanceof ItemDAO item) {
            return Optional.of(item);
        } else if (value instanceof Map) {
            final Map<String, Object> map = (Map<String, Object>) value;
            return Optional.of(new ItemDAO(((Number) map.get("id")).longValue(),
                    (String) map.get("title"),
                    (String) map.get("description"),
                    (String) map.get("imgPath"),
                    ((Number) map.get("price")).longValue(),
                    ((Number) map.get("count")).intValue()));

        }
        return Optional.empty();
    }
}
