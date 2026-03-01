package ru.yandex.practicum.mymarket.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
public abstract class AbstractController {
    @Autowired
    protected WebTestClient webTestClient;
    protected final String sessionId = "sessionId";
}
