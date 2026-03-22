package ru.ya.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import ru.ya.practicum.mymarket.controllers.ItemController;
import ru.ya.practicum.mymarket.repositories.dao.ItemDAO;

import java.time.Duration;
import java.util.List;

@Testcontainers
public abstract class AbstractTestWithRedis extends AbstractTest {

    @Container
    static GenericContainer<?> redis;

    static {
        redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379)
                .withReuse(true);

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
    protected ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;


    protected List<ItemDAO> mockItems;
    protected static final String SEARCH = "ite";
    protected static final ItemController.SortMethod SORT_METHOD = ItemController.SortMethod.ALPHA;

    @BeforeEach
    void setUp() {
        reactiveRedisTemplate.execute(conn -> conn.serverCommands().flushAll())
                .timeout(Duration.ofSeconds(5))
                .doOnError(e -> System.err.println("Flush error: " + e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .blockLast();
    }


}
