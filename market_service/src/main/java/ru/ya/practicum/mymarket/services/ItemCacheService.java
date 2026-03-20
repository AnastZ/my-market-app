package ru.ya.practicum.mymarket.services;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.controllers.ItemController;
import ru.ya.practicum.mymarket.repositories.ItemRepository;
import ru.ya.practicum.mymarket.repositories.dao.ItemDAO;

import java.util.List;

@Service
public class ItemCacheService {
    private final Logger log = LoggerFactory.getLogger(ItemCacheService.class);
    private static final String CACHE_KEY_PREFIX = "items:";
    private final ItemRepository itemRepository;
    private final ReactiveRedisTemplate<Object, Object> reactiveRedisTemplate;

    public ItemCacheService(@NotNull final ItemRepository itemRepository,
                            @NotNull final ReactiveRedisTemplate<Object, Object> reactiveRedisTemplate) {
        this.itemRepository = itemRepository;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    /**
     * Кэшируемый метод - вынесен в отдельный сервис
     */

    public Flux<ItemDAO> getAllCached(@NotNull final String search,
                                      @NotNull final ItemController.SortMethod sortMethod,
                                      @NotNull final String sessionId) {
        String cacheKey = buildCacheKey(search, sortMethod, sessionId);

        return reactiveRedisTemplate.opsForValue()
                .get(cacheKey)
                .flatMapMany(value -> {
                    // Если значение найдено в кэше
                    if (value instanceof List<?>) {
                        List<?> list = (List<?>) value;
                        if (!list.isEmpty() && list.get(0) instanceof ItemDAO) {
                            log.debug("Cache hit for key: {}", cacheKey);

                            List<ItemDAO> items = (List<ItemDAO>) list;
                            return Flux.fromIterable(items);
                        }
                    }

                    // Если формат неверный
                    log.debug("Invalid cache format for key: {}", cacheKey);
                    return loadAndCache(search, sortMethod, sessionId);
                })
                .switchIfEmpty(loadAndCache(search, sortMethod, sessionId));
    }

    private Flux<ItemDAO> loadAndCache(String search,
                                       ItemController.SortMethod sortMethod,
                                       String sessionId) {
        return loadFromDatabase(search, sortMethod, sessionId)
                .collectList()
                .flatMapMany(itemList ->
                        reactiveRedisTemplate.opsForValue()
                                .set(buildCacheKey(search, sortMethod, sessionId), itemList, 2)
                                .thenMany(Flux.fromIterable(itemList))
                );
    }

    private Flux<ItemDAO> loadFromDatabase(String search,
                                           ItemController.SortMethod sortMethod,
                                           String sessionId) {
        final Sort sort = switch (sortMethod) {
            case ALPHA -> Sort.by("title");
            case PRICE -> Sort.by("price");
            default -> Sort.unsorted();
        };
        return itemRepository.findAllWithCart(search, sessionId, sort);
    }

    public static String buildCacheKey(@NotNull final String search,
                                       @NotNull final ItemController.SortMethod sortMethod,
                                       @NotNull final String sessionId) {
        return String.format("%s%s:%s:%s", CACHE_KEY_PREFIX, search, sortMethod.name(), sessionId);
    }

    /**
     * Инвалидировать весь кэш товаров.
     */
    Mono<Void> evictItemsCache() {
        return reactiveRedisTemplate.keys(CACHE_KEY_PREFIX + "*")
                .flatMap(reactiveRedisTemplate.opsForValue()::delete)
                .then()
                .doOnSuccess(v -> log.debug("Items cache evicted"));
    }

}
