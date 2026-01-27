package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ItemInMemoryStorage implements ItemStorage {

    private static Long itemId = 0L;
    private final HashMap<Long, Item> itemsMap = new HashMap<>();

    @Override
    public Item create(Item item, Long userId) {
        item.setId(nextId());
        item.setOwner(userId);
        itemsMap.put(item.getId(), item);

        return Item.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(item.getOwner())
                .request(item.getRequest())
                .build();
    }

    @Override
    public Item update(Long id, Item item) {
         itemsMap.put(id, item);
         return item;
    }

    @Override
    public Optional<Item> getItemById(Long id) {
        return Optional.ofNullable(itemsMap.get(id))
                .map(item -> Item.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .available(item.getAvailable())
                        .owner(item.getOwner())
                        .request(item.getRequest())
                        .build());
    }

    @Override
    public List<Item> getAllItemsByUser(Long userId) {
        return itemsMap.values().stream()
                .filter(item -> item.getOwner().equals(userId))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Item> searchItem(String text) {
        final String searchText = text.trim().toLowerCase();

        return itemsMap.values().stream()
                .filter(item -> item.getAvailable())
                .filter(item ->
                        item.getName().toLowerCase().contains(searchText) ||
                                item.getDescription().toLowerCase().contains(searchText))
                .collect(Collectors.toUnmodifiableList());
    }

    private static Long nextId() {
        return ++itemId;
    }
}
