package ru.ya.practicum.mymarket.integration.controllers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.ya.practicum.mymarket.integration.AbstractTestWithRedis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
public class OrderControllerTest extends AbstractTestWithRedis {

    private final String path = "/orders";

    @Test
    public void getAll_success() {
        webTestClient.get()
                .uri(path)
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html")
                .expectBody(String.class)
                .consumeWith(result -> {
                    final Document doc = Jsoup.parse(result.getResponseBody());

                    final Elements orderCards = doc.select(".card");
                    assertThat(orderCards).isNotEmpty();

                    final Element firstOrder = orderCards.first();

                    final Element orderLink = firstOrder.selectFirst(".card-header a");
                    assertThat(orderLink).isNotNull();
                    assertThat(orderLink.text()).startsWith("Заказ №");
                    assertThat(orderLink.attr("href")).startsWith("/orders/");

                    final Elements items = firstOrder.select(".list-group-item");
                    assertThat(items).isNotEmpty();

                    final Element firstItem = items.first();
                    assertNotNull(firstItem);
                    final String itemText = firstItem.text();
                    assertThat(itemText).containsPattern(".+ \\(\\d+ шт\\.\\).+ руб\\.");

                    assertThat(itemText).containsPattern("\\(\\d+ шт\\.\\)");

                    assertThat(itemText).contains("руб.");

                    final Element footer = firstOrder.selectFirst(".card-footer");
                    assertThat(footer).isNotNull();
                    final String footerText = footer.text();
                    assertThat(footerText).startsWith("Сумма:");
                    assertThat(footerText).contains("руб.");

                    final String sumStr = footerText.replaceAll("[^0-9]", "");
                    assertThat(sumStr).isNotEmpty();
                    final int sum = Integer.parseInt(sumStr);
                    assertThat(sum).isPositive();
                });
    }

}
