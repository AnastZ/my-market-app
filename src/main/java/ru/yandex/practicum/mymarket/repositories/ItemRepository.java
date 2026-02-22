package ru.yandex.practicum.mymarket.repositories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SearchResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repositories.dao.ItemDAO;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {



    @Query("SELECT new ru.yandex.practicum.mymarket.repositories.dao.ItemDAO(item, COALESCE(ci.count, 0L)) " +
            " FROM Item item " +
            " LEFT JOIN CartItem ci ON ci.item = item" +
            " WHERE (LOWER(item.title) LIKE LOWER(CONCAT('%', :search, '%'))" +
            " OR LOWER(item.description) LIKE LOWER(CONCAT('%', :search, '%'))) ")
    Page<ItemDAO> findAll(@NotNull String search,
                          @NotNull @NotBlank String sessionId,
                          @NotNull PageRequest of);
}
