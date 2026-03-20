package ru.ya.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import ru.ya.practicum.mymarket.controllers.ItemController;
import ru.ya.practicum.mymarket.repositories.ItemRepository;
import ru.ya.practicum.mymarket.repositories.dao.ItemDAO;
import ru.ya.practicum.mymarket.services.ItemCacheService;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
@Testcontainers
@Import(Config.class)
public class ItemCacheTests extends AbstractTest implements FillCart{
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private ItemCacheService itemCacheService;

    @MockitoBean
    private ItemRepository itemRepository;

    @Autowired
    private ReactiveRedisTemplate<Object, Object> reactiveRedisTemplate;


    private static final String SEARCH = "test";
    private static final ItemController.SortMethod SORT_METHOD = ItemController.SortMethod.ALPHA;

    private List<ItemDAO> mockItems;

    @BeforeEach
    void setUp() {
        mockItems = Arrays.asList(
                new ItemDAO(1L, "Item 1", "Description 1", "img", 100L, 1),
                new ItemDAO(2L, "Item 2", "Description 2", "img", 100L, 1),
                new ItemDAO(3L, "Item 3", "Description 3", "img", 100L, 1)
        );

        // Очищаем кэш перед каждым тестом
        reactiveRedisTemplate.keys("items:*")
                .flatMap(reactiveRedisTemplate.opsForValue()::delete)
                .blockLast();
    }

    @Test
    void getAllCached_CacheMiss_ShouldLoadFromDatabaseAndCache() {

        when(itemRepository.findAllWithCart(eq(SEARCH), eq(sessionId), any(Sort.class)))
                .thenReturn(Flux.fromIterable(mockItems));

        final List<ItemDAO> result = itemCacheService.getAllCached(SEARCH, SORT_METHOD, sessionId)
                .collectList()
                .block();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(mockItems);

        verify(itemRepository, times(1))
                .findAllWithCart(eq(SEARCH), eq(sessionId), any(Sort.class));

        String cacheKey = ItemCacheService.buildCacheKey(SEARCH, SORT_METHOD, sessionId);
        Object cached = reactiveRedisTemplate.opsForValue().get(cacheKey).block();
        assertThat(cached).isInstanceOf(List.class);
        assertThat((List<?>) cached).hasSize(3);
    }

    @Test
    void getAllCached_fromCache_success() {

        final String cacheKey = "items:" + SEARCH + ":" + SORT_METHOD.name() + ":" + sessionId;
        reactiveRedisTemplate.opsForValue()
                .set(cacheKey, mockItems, Duration.ofMinutes(30))
                .block();
        final List<ItemDAO> result = itemCacheService.getAllCached(SEARCH, SORT_METHOD, sessionId)
                .collectList()
                .block();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(mockItems);

        verify(itemRepository, never())
                .findAllWithCart(anyString(), anyString(), any(Sort.class));
    }

    @Test
    void getAllCached_EmptyResult_ShouldCacheEmptyList() {
        when(itemRepository.findAllWithCart(eq(SEARCH), eq(sessionId), any(Sort.class)))
                .thenReturn(Flux.empty());

        final List<ItemDAO> result = itemCacheService.getAllCached(SEARCH, SORT_METHOD, sessionId)
                .collectList()
                .block();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        String cacheKey = "items:" + SEARCH + ":" + SORT_METHOD.name() + ":" + sessionId;
        Object cached = reactiveRedisTemplate.opsForValue().get(cacheKey).block();
        assertThat(cached).isInstanceOf(List.class);
        assertThat((List<?>) cached).isEmpty();
    }

    @Test
    void getAllCached_DifferentSortMethods_ShouldCacheSeparately() {
        // Given - Настраиваем мок для ALPHA сортировки
        when(itemRepository.findAllWithCart(eq("test"), eq("session123"), argThat(sort ->
                sort != null && sort.getOrderFor("title") != null
        ))).thenReturn(Flux.fromIterable(mockItems));

        // Настраиваем мок для PRICE сортировки
        when(itemRepository.findAllWithCart(eq("test"), eq("session123"), argThat(sort ->
                sort != null && sort.getOrderFor("price") != null
        ))).thenReturn(Flux.fromIterable(mockItems));

        // When - Вызываем метод для ALPHA сортировки
        List<ItemDAO> resultAlpha = itemCacheService.getAllCached("test", ItemController.SortMethod.ALPHA, "session123")
                .collectList()
                .block();

        // When - Вызываем метод для PRICE сортировки
        List<ItemDAO> resultPrice = itemCacheService.getAllCached("test", ItemController.SortMethod.PRICE, "session123")
                .collectList()
                .block();

        // Then - Проверяем результаты
        assertThat(resultAlpha).isNotNull();
        assertThat(resultAlpha).hasSize(3);
        assertThat(resultAlpha).isEqualTo(mockItems);

        assertThat(resultPrice).isNotNull();
        assertThat(resultPrice).hasSize(3);
        assertThat(resultPrice).isEqualTo(mockItems);

        // Проверяем, что кэш создан для разных ключей
        String alphaKey = "items:test:ALPHA:session123";
        String priceKey = "items:test:PRICE:session123";

        Boolean alphaExists = reactiveRedisTemplate.hasKey(alphaKey).block();
        Boolean priceExists = reactiveRedisTemplate.hasKey(priceKey).block();

        assertThat(alphaExists).isTrue();
        assertThat(priceExists).isTrue();

        // Проверяем содержимое кэша
        Object alphaCached = reactiveRedisTemplate.opsForValue().get(alphaKey).block();
        Object priceCached = reactiveRedisTemplate.opsForValue().get(priceKey).block();

        assertThat(alphaCached).isInstanceOf(List.class);
        assertThat((List<?>) alphaCached).hasSize(3);

        assertThat(priceCached).isInstanceOf(List.class);
        assertThat((List<?>) priceCached).hasSize(3);

        // Проверяем, что репозиторий был вызван 2 раза (по разу для каждого метода сортировки)
        verify(itemRepository, times(2))
                .findAllWithCart(eq("test"), eq("session123"), any(Sort.class));
    }

}
