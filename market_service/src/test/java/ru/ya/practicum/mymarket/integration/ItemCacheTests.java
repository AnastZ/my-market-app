package ru.ya.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.controllers.dto.EntityConvertor;
import ru.ya.practicum.mymarket.model.Item;
import ru.ya.practicum.mymarket.model.SortMethod;
import ru.ya.practicum.mymarket.repositories.ItemRepository;
import ru.ya.practicum.mymarket.model.ItemWithCartCount;
import ru.ya.practicum.mymarket.services.ItemCacheService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@Import(Config.class)
public class ItemCacheTests extends AbstractTestWithRedis implements FillCart {
    protected List<ItemWithCartCount> mockItems;
    private static final String CACHE_ITEMS_LIST = "itemsList";
    private static final String CACHE_ITEMS_CART = "itemsCart";
    private static final String CACHE_ITEM = "item";

    @Value("${item.list-size}")
    private int listSize;

    @MockitoBean
    protected ItemRepository itemRepository;

    @Autowired
    private EntityConvertor<ItemWithCartCount, Item> itemConvertor;

    @BeforeEach
    @Override
    void setUp() {
        mockItems = Arrays.asList(
                new ItemWithCartCount(1L, "Item 1", "Description 1", "img", 100L, 1),
                new ItemWithCartCount(2L, "Item 2", "Description 2", "img", 100L, 1),
                new ItemWithCartCount(3L, "Item 3", "Description 3", "img", 100L, 1)
        );
        super.setUp();
        when(itemRepository.findAllWithCart(anyString(), anyString(), any(Sort.class)))
                .thenReturn(Flux.fromIterable(mockItems));
        when(itemRepository.findAll())
                .thenReturn(Flux.fromIterable(List.of(itemConvertor.convert(mockItems.get(0)))));
    }

    @Autowired
    private ItemCacheService itemCacheService;
    @Test
    void getAllCached_firstQueryFromDB_afterFromCache_success() {
        final List<ItemWithCartCount> result = itemCacheService.getAllCached(SEARCH, SORT_METHOD, username)
                .collectList()
                .block();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(listSize);
        assertThat(result).isEqualTo(mockItems);

        verify(itemRepository, times(1))
                .findAllWithCart(eq(SEARCH), eq(username), any(Sort.class));

        final Cache cache = cacheManager.getCache(CACHE_ITEMS_LIST);
        assertThat(cache).isNotNull();
        String cacheKey = ItemCacheService.getCacheKey(SEARCH, SORT_METHOD.name(), username);

        @SuppressWarnings("unchecked")
        final List<ItemWithCartCount> cachedItems = cache.get(cacheKey, List.class);
        assertThat(cachedItems).isNotNull();
        assertThat(cachedItems).hasSize(3);
        assertThat(cachedItems).isEqualTo(mockItems);
    }

    @Test
    void getAllCached_fromCache_success() {
        final String cacheKey = ItemCacheService.getCacheKey(SEARCH, SORT_METHOD.name(), username);
        org.springframework.cache.Cache cache = cacheManager.getCache(CACHE_ITEMS_LIST);
        assertThat(cache).isNotNull();
        cache.put(cacheKey, mockItems);

        final List<ItemWithCartCount> result = itemCacheService.getAllCached(SEARCH, SORT_METHOD, username)
                .collectList()
                .block();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(listSize);
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

        // Act
        final List<ItemWithCartCount> resultAlpha = itemCacheService.getAllCached("test", SortMethod.ALPHA, "session123")
                .collectList()
                .block();

        final List<ItemWithCartCount> resultPrice = itemCacheService.getAllCached("test", SortMethod.PRICE, "session123")
                .collectList()
                .block();

        // Assert
        assertThat(resultAlpha).isNotNull();
        assertThat(resultAlpha).hasSize(3);
        assertThat(resultAlpha).isEqualTo(mockItems);

        assertThat(resultPrice).isNotNull();
        assertThat(resultPrice).hasSize(3);
        assertThat(resultPrice).isEqualTo(mockItems);


        final String alphaKey = "test:" + SortMethod.ALPHA.name() + ":session123";
        final String priceKey = "test:" + SortMethod.PRICE.name() + ":session123";

        org.springframework.cache.Cache cache = cacheManager.getCache(CACHE_ITEMS_LIST);
        assertThat(cache).isNotNull();

        assertThat(cache.get(alphaKey)).isNotNull();
        assertThat(cache.get(priceKey)).isNotNull();

        @SuppressWarnings("unchecked")
        final List<ItemWithCartCount> alphaCached = cache.get(alphaKey, List.class);
        @SuppressWarnings("unchecked")
        final List<ItemWithCartCount> priceCached = cache.get(priceKey, List.class);

        assertThat(alphaCached).hasSize(3);
        assertThat(priceCached).hasSize(3);

        verify(itemRepository, times(2))
                .findAllWithCart(eq("test"), eq("session123"), any(Sort.class));
    }

    @Test
    void getAllCached_CartItems_CacheMiss_ShouldLoadFromDatabaseAndCache() {
        final String cartSessionId = "cart-session-123";
        when(itemRepository.findAllInCart(eq(cartSessionId)))
                .thenReturn(Flux.fromIterable(mockItems));

        final List<ItemWithCartCount> result = itemCacheService.getAllCached(cartSessionId)
                .collectList()
                .block();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(mockItems);

        verify(itemRepository, times(1))
                .findAllInCart(eq(cartSessionId));

        org.springframework.cache.Cache cache = cacheManager.getCache(CACHE_ITEMS_CART);
        assertThat(cache).isNotNull();

        @SuppressWarnings("unchecked")
        final List<ItemWithCartCount> cachedItems = cache.get(cartSessionId, List.class);
        assertThat(cachedItems).isNotNull();
        assertThat(cachedItems).hasSize(3);
    }

    @Test
    void getAllCached_CartItems_FromCache_Success() {
        final String cartSessionId = "cart-session-456";
        org.springframework.cache.Cache cache = cacheManager.getCache(CACHE_ITEMS_CART);
        assertThat(cache).isNotNull();
        cache.put(cartSessionId, mockItems);

        // Act
        final List<ItemWithCartCount> result = itemCacheService.getAllCached(cartSessionId)
                .collectList()
                .block();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(mockItems);

        verify(itemRepository, never())
                .findAllInCart(anyString());
    }

    @Test
    void getById_CacheMiss_ShouldLoadFromDatabaseAndCache() {
        final Long itemId = itemRepository.findAll().blockFirst().getId();
        final String sessionId = "session-123";
        final ItemWithCartCount expectedItem = new ItemWithCartCount(itemId, "Item 1", "Description", "img", 100L, 1);

        when(itemRepository.findByIdAndSessionId(eq(itemId), eq(sessionId)))
                .thenReturn(Mono.just(expectedItem));

        final ItemWithCartCount result = itemCacheService.getById(itemId, sessionId)
                .block();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedItem);

        verify(itemRepository, times(1))
                .findByIdAndSessionId(eq(itemId), eq(sessionId));

        final String cacheKey = itemId + ":" + sessionId;
        org.springframework.cache.Cache cache = cacheManager.getCache(CACHE_ITEM);
        assertThat(cache).isNotNull();

        final ItemWithCartCount cachedItem = cache.get(cacheKey, ItemWithCartCount.class);
        assertThat(cachedItem).isNotNull();
        assertThat(cachedItem).isEqualTo(expectedItem);
    }
}
