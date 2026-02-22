package ru.yandex.practicum.mymarket.services;

import jakarta.persistence.NoResultException;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.controllers.ItemController;
import ru.yandex.practicum.mymarket.controllers.dto.DTOConvertor;
import ru.yandex.practicum.mymarket.controllers.dto.ItemDTO;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.controllers.dto.ItemsDTO;
import ru.yandex.practicum.mymarket.model.Paging;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.repositories.dao.ItemDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final int listSize;
    private final DTOConvertor<ItemDAO, ItemDTO> itemDTOConvertor;

    public ItemService(@NotNull final ItemRepository itemRepository,
                       @Value("${item.list-size}") @NotNull final int listSize,
                       @NotNull final DTOConvertor<ItemDAO, ItemDTO> itemDTOConvertor) {
        this.itemRepository = itemRepository;
        this.listSize = listSize;
        this.itemDTOConvertor = itemDTOConvertor;
    }

    /**
     * Разделить список объектов на множество списков с одинаковой длиной,
     * если не хватает объектов для заполнения последнего списка,
     * то добавить объекты-заглушки (пустой объект Item с id = -1L).
     *
     * @param items      список объектов.
     * @param nestedSize размер вложенного списка.
     * @return список из списков.
     */
    private @NotNull List<List<ItemDTO>> convertToNestedLists(@NotNull @NotEmpty final List<ItemDTO> items,
                                                              @Min(1) final int nestedSize) {
        final List<ItemDTO> modifyItems = new ArrayList<>(items);
        final int shortage = nestedSize - (items.size() % nestedSize);
        for (int i = 0; i < shortage; i++) {
            modifyItems.add(ItemDTO.ofSpecial());
        }
        final List<List<ItemDTO>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (final ItemDTO item : modifyItems) {
            if (result.getLast().size() >= nestedSize) {
                result.add(new ArrayList<>());
            }
            result.getLast().add(item);
        }
        return result;
    }

    /**
     * Получить страницу объектов из БД с сортировкой.
     *
     * @param pageNumber номер страницы.
     * @param pageSize   количество объектов на странице.
     * @param search     поисковой запрос (фильтрация по названию/описанию), если без фильтрации, то следует передать пустую строку, значение null неприемлимо.
     * @param sortMethod метод сортировки.
     * @return список объектов.
     */
    private @NotNull Page<ItemDAO> getAll(@Min(1) final int pageNumber,
                                          @Min(1) final int pageSize,
                                          @NotNull final String search,
                                          @NotNull final ItemController.SortMethod sortMethod,
                                          @NotNull @NotBlank final String sessionId) {
        switch (sortMethod) {
            case ALPHA -> {
                return itemRepository.findAll(search, sessionId, PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "title")));
            }
            case PRICE -> {
                return itemRepository.findAll(search, sessionId, PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "price")));
            }
            default -> {
                return itemRepository.findAll(search, sessionId, PageRequest.of(pageNumber, pageSize));
            }
        }
    }

    /**
     * Получить объекты из БД.
     *
     * @param pageNumber номер страницы.
     * @param pageSize   количество объектов на странице.
     * @param search     поисковой запрос (фильтрация по названию/описанию), если фильтрация не нужна, то передать либо null, либо пустую строку.
     * @param sortMethod метод сортировки.
     * @return найденные объекты.
     * @throws NoResultException если объекты не найдены.
     */
    @Transactional(readOnly = true)
    public @NotNull ItemsDTO getItems(@Min(1) final int pageNumber,
                                      @Min(1) final int pageSize,
                                      final String search,
                                      @NotNull final ItemController.SortMethod sortMethod,
                                      @NotNull @NotBlank final String sessionId) throws NoResultException {
        final Page<ItemDAO> items = getAll(pageNumber, pageSize, Objects.isNull(search) ? "" : search, sortMethod, sessionId);
        if (items.isEmpty()) {
            throw new NoResultException("No items found.");
        }

        return new ItemsDTO(convertToNestedLists(items.getContent().stream().map(itemDTOConvertor::toDTO).toList(), listSize),
                new Paging(pageNumber, pageSize, pageNumber != 1, !items.isLast()));
    }

}














