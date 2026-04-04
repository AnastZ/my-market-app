package ru.ya.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.ya.practicum.mymarket.controllers.ItemController;
import ru.ya.practicum.mymarket.integration.config.SecurityConfig;
import ru.ya.practicum.mymarket.integration.config.SecurityConfigSimple;
import ru.ya.practicum.mymarket.model.SortMethod;

import java.util.Objects;

@Testcontainers
public abstract class AbstractTestWithRedis extends AbstractTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);;

    static {
        if (!redis.isRunning()) {
            redis.start();
        }
    }


    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }
    @Autowired
    protected CacheManager cacheManager;

    protected static final String SEARCH = "ite";
    protected static final SortMethod SORT_METHOD = SortMethod.ALPHA;


    @BeforeEach
    protected void setUp() {
        clearAllCaches();
    }

    private void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
        });
    }
}
