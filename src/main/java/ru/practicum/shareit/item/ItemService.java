package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemDbStorage;
import ru.practicum.shareit.validation.ValidationTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemDbStorage itemDbStorage;
    private static final String PROGRAM_LEVEL = "ItemService";

    public ItemDto create(ItemDto itemDto, Long user_id) {
        Item itemCreate = ItemDtoMapper.toItem(itemDto);
        ValidationTool.checkId(user_id, PROGRAM_LEVEL, "при создании вещи user_id не должен равняться null");
        if (itemCreate.getName() == null || itemCreate.getName().isBlank()) {
            throw new InternalServerException("имя вещи не может быть пустым");
        }
        return ItemDtoMapper.toItemDto(itemDbStorage.create(itemCreate, user_id));
    }

    public ItemDto update(Long id, ItemDto itemDto, Long user_id) {
        ValidationTool.checkId(id, PROGRAM_LEVEL, "вещь не может быть обновлена по id = null");

        Item item1 = itemDbStorage.getItemById(id);

        Item updateItem = Item.builder()
            .id(id)
            .name(itemDto.getName() != null ? itemDto.getName() : item1.getName())
            .description(itemDto.getDescription() != null ? itemDto.getDescription() : item1.getDescription())
            .available(itemDto.getAvailable() != null ? itemDto.getAvailable() : item1.getAvailable())
            .owner(itemDto.getOwner() != null ? itemDto.getOwner() : item1.getOwner())
            .request(itemDto.getRequest() != null ? itemDto.getRequest() : item1.getRequest())
            .build();

        itemDbStorage.update(id, updateItem, user_id);
        return ItemDtoMapper.toItemDto(updateItem);
    }

    public ItemDto getItemById(Long id) {
        ValidationTool.checkId(id, PROGRAM_LEVEL, "вещь не может быть найдена по id = null");
        return ItemDtoMapper.toItemDto(itemDbStorage.getItemById(id));
    }

    public List<ItemDto> getAllItemsByUser(Long userId) {
        ValidationTool.checkId(userId, PROGRAM_LEVEL, "вещи не могут быть найдена по id_user = null");
        List<ItemDto> allItemDto = new ArrayList<>();
        for (Item item : itemDbStorage.getAllItemsByUser(userId)) {
            allItemDto.add(ItemDtoMapper.toItemDto(item));
        }
        return allItemDto;
    }

    public List<ItemDto> searchItem(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemDto> allItemDto = new ArrayList<>();
        for (Item item : itemDbStorage.searchItem(text)) {
            allItemDto.add(ItemDtoMapper.toItemDto(item));
        }
        return allItemDto;
    }
}