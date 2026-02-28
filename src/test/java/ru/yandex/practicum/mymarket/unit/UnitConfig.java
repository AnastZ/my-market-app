package ru.yandex.practicum.mymarket.unit;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.yandex.practicum.mymarket.controllers.dto.DTOConfig;
import ru.yandex.practicum.mymarket.controllers.dto.DTOConvertor;
import ru.yandex.practicum.mymarket.controllers.dto.ItemDTO;
import ru.yandex.practicum.mymarket.repositories.dao.ItemDAO;

@TestConfiguration
public class UnitConfig {
    @Bean
    public DTOConvertor<ItemDAO, ItemDTO> itemDTOConvertor() {
        return new DTOConfig().itemDTOConvertor();
    }
}
