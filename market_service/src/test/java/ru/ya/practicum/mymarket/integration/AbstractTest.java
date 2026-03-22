package ru.ya.practicum.mymarket.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

@SpringBootTest
@AutoConfigureWebTestClient
public abstract class AbstractTest {
    @Autowired
    protected WebTestClient webTestClient;
    protected final String sessionId = String.valueOf(UUID.randomUUID());
}
