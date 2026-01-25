package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {

    Item create(Item item, Long userId);

    Item update(Long id, Item item, Long userId);

    Item getItemById(Long id);

    List<Item> getAllItemsByUser(Long userId);

    List<Item> searchItem(String text);
}
