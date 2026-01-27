package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.validation.ValidationTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserService userService;
    private static final String PROGRAM_LEVEL = "ItemService";

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        Item itemCreate = ItemDtoMapper.toItem(itemDto);
        ValidationTool.checkId(userId, PROGRAM_LEVEL, "при создании вещи user_id не должен равняться null");

        if (itemCreate.getName() == null || itemCreate.getName().isBlank()) {
            throw new InternalServerException("имя вещи не может быть пустым");
        }
        if (userService.getUserById(userId) == null) {
            throw new NotFoundException("не найден владелец вещи по id = " + userId);
        }
        if (itemCreate.getAvailable() == null) {
            throw new InternalServerException("заполните занятость вещи");
        }
        if (itemCreate.getDescription() == null) {
            throw new InternalServerException("описание вещи не может равняться null");
        }
        return ItemDtoMapper.toItemDto(itemStorage.create(itemCreate, userId));
    }

    @Override
    public ItemDto update(Long id, ItemDto itemDto, Long userId) {
        ValidationTool.checkId(id, PROGRAM_LEVEL, "вещь не может быть обновлена по id = null");

        Item item1 = itemStorage.getItemById(id).orElseThrow(
                () -> new NotFoundException("вещь с id = " + id + " не найдена")
        );

        if (!item1.getOwner().equals(userId)) {
            throw new NotFoundException("id владельца не совпадает с передаваемым id");
        }

        Item updateItem = Item.builder()
            .id(id)
            .name(itemDto.getName() != null ? itemDto.getName() : item1.getName())
            .description(itemDto.getDescription() != null ? itemDto.getDescription() : item1.getDescription())
            .available(itemDto.getAvailable() != null ? itemDto.getAvailable() : item1.getAvailable())
            .owner(itemDto.getOwner() != null ? itemDto.getOwner() : item1.getOwner())
            .request(itemDto.getRequest() != null ? itemDto.getRequest() : item1.getRequest())
            .build();

        itemStorage.update(id, updateItem);
        return ItemDtoMapper.toItemDto(updateItem);
    }

    @Override
    public ItemDto getItemById(Long id) {
        ValidationTool.checkId(id, PROGRAM_LEVEL, "вещь не может быть найдена по id = null");
        Item item = itemStorage.getItemById(id).orElseThrow(
                () -> new NotFoundException("вещь с id = " + id + " не найдена")
        );
        return ItemDtoMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllItemsByUser(Long userId) {
        ValidationTool.checkId(userId, PROGRAM_LEVEL, "вещи не могут быть найдена по id_user = null");
        List<ItemDto> allItemDto = new ArrayList<>();
        for (Item item : itemStorage.getAllItemsByUser(userId)) {
            allItemDto.add(ItemDtoMapper.toItemDto(item));
        }
        return allItemDto;
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Поиск вещей, имя или описание которых содержат: {}.", text);

        return itemStorage.searchItem(text).stream()
                .map(ItemDtoMapper::toItemDto)
                .collect(Collectors.toUnmodifiableList());
    }
}