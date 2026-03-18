package ru.yandex.practicum.mymarket.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

@SpringBootTest
@AutoConfigureWebTestClient
public abstract class AbstractController {
    @Autowired
    protected WebTestClient webTestClient;
    protected final String sessionId = String.valueOf(UUID.randomUUID());
}
