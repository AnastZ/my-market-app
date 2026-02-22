package ru.yandex.practicum.mymarket.controllers.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repositories.dao.ItemDAO;

@Configuration
public class DTOConfig {
    @Bean
    public DTOConvertor<ItemDAO, ItemDTO> itemDTOConvertor(){
        return item -> {
            final Item i = item.getItem();
            return new ItemDTO(i.getId(), i.getTitle(), i.getDescription(), i.getImgPath(), i.getPrice(), item.getCount());
        };
    }
}
