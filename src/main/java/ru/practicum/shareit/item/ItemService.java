package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(Long id, ItemDto itemDto, Long userId);

    ItemDto getItemById(Long id);

    List<ItemDto> getAllItemsByUser(Long userId);

    List<ItemDto> searchItem(String text);
}
