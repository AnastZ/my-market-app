package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.controllers.ItemController;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.services.ItemService;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest

public class ItemServiceTest {

    @Autowired
    ItemService itemService;

    @Autowired
    ItemRepository itemRepository;

    @Transactional
    @BeforeEach
    public void setUp() throws Exception {
        final List<Item> items = new ArrayList<>();
        for(long i = 1; i<10; i++){
            items.add(new Item("item" + i, "desc" + i, "imgpath", i));
        }
        itemRepository.saveAll(items);
    }

    @Test
    public void testFindAll() {
//        itemService.getAll(1, 1, "", ItemController.SortMethod.NO).forEach(System.out::println);
    }
}
