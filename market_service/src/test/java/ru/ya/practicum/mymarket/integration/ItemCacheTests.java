package ru.ya.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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


@Import(Config.class)
public class ItemCacheTests extends AbstractTestWithRedis implements FillCart {

    @MockitoBean
    protected ItemRepository itemRepository;

    protected String getCacheKey() {
        return ItemCacheService.buildCacheKeyForList(SEARCH, SORT_METHOD);
    }
    @BeforeEach
    @Override
    void setUp() {
        mockItems = Arrays.asList(
                new ItemDAO(1L, "Item 1", "Description 1", "img", 100L, 1),
                new ItemDAO(2L, "Item 2", "Description 2", "img", 100L, 1),
                new ItemDAO(3L, "Item 3", "Description 3", "img", 100L, 1)
        );
        super.setUp();
        when(itemRepository.findAllWithCart(anyString(), anyString(), any(Sort.class)))
                .thenReturn(Flux.fromIterable(mockItems));

    }

    @Autowired
    private ItemCacheService itemCacheService;
    @Test
    void getAllCached_CacheMiss_ShouldLoadFromDatabaseAndCache() {
        final List<ItemDAO> result = itemCacheService.getAllCached(SEARCH, SORT_METHOD, sessionId)
                .collectList()
                .block();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(mockItems);

        verify(itemRepository, times(1))
                .findAllWithCart(eq(SEARCH), eq(sessionId), any(Sort.class));

        final Object cached = reactiveRedisTemplate.opsForValue().get(getCacheKey()).block();
        assertThat(cached).isInstanceOf(List.class);
        assertThat((List<?>) cached).hasSize(3);
    }

    @Test
    void getAllCached_fromCache_success() {

        reactiveRedisTemplate.opsForValue()
                .set(getCacheKey(), mockItems, Duration.ofMinutes(30))
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
    void getAllCached_DifferentSortMethods_ShouldCacheSeparately() {
        when(itemRepository.findAllWithCart(eq("test"), eq("session123"), argThat(sort ->
                sort != null && sort.getOrderFor("title") != null
        ))).thenReturn(Flux.fromIterable(mockItems));

        when(itemRepository.findAllWithCart(eq("test"), eq("session123"), argThat(sort ->
                sort != null && sort.getOrderFor("price") != null
        ))).thenReturn(Flux.fromIterable(mockItems));

        final List<ItemDAO> resultAlpha = itemCacheService.getAllCached("test", ItemController.SortMethod.ALPHA, "session123")
                .collectList()
                .block();

        final List<ItemDAO> resultPrice = itemCacheService.getAllCached("test", ItemController.SortMethod.PRICE, "session123")
                .collectList()
                .block();

        assertThat(resultAlpha).isNotNull();
        assertThat(resultAlpha).hasSize(3);
        assertThat(resultAlpha).isEqualTo(mockItems);

        assertThat(resultPrice).isNotNull();
        assertThat(resultPrice).hasSize(3);
        assertThat(resultPrice).isEqualTo(mockItems);

        final String alphaKey = "items:test:ALPHA";
        final String priceKey = "items:test:PRICE";

        final Boolean alphaExists = reactiveRedisTemplate.hasKey(alphaKey).block();
        final Boolean priceExists = reactiveRedisTemplate.hasKey(priceKey).block();

        assertThat(alphaExists).isTrue();
        assertThat(priceExists).isTrue();

        final Object alphaCached = reactiveRedisTemplate.opsForValue().get(alphaKey).block();
        final Object priceCached = reactiveRedisTemplate.opsForValue().get(priceKey).block();

        assertThat(alphaCached).isInstanceOf(List.class);
        assertThat((List<?>) alphaCached).hasSize(3);

        assertThat(priceCached).isInstanceOf(List.class);
        assertThat((List<?>) priceCached).hasSize(3);

        verify(itemRepository, times(2))
                .findAllWithCart(eq("test"), eq("session123"), any(Sort.class));
    }

}
