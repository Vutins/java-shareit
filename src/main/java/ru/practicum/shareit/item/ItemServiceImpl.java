package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.validation.ValidationTool;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repository;
    private final UserService userService;
    private final ItemMapper itemMapper;
    private static final String PROGRAM_LEVEL = "ItemService";

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        Item itemCreate = itemMapper.toEntity(itemDto);
        itemCreate.setOwner(userId);
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

        return itemMapper.toDto(repository.save(itemCreate));
    }

    @Override
    public ItemDto update(Long id, ItemDto itemDto, Long userId) {
        ValidationTool.checkId(id, PROGRAM_LEVEL, "вещь не может быть обновлена по id = null");

        Item existingItem = repository.findById(id).orElseThrow(
                () -> new NotFoundException("вещь с id = " + id + " не найдена")
        );

        if (!existingItem.getOwner().equals(userId)) {
            throw new NotFoundException("id владельца не совпадает с передаваемым id");
        }

        itemMapper.updateItemFromDto(itemDto, existingItem);
        existingItem.setId(id);

        repository.save(existingItem);
        return itemMapper.toDto(existingItem);
    }

    @Override
    public ItemDto getItemById(Long id) {
        ValidationTool.checkId(id, PROGRAM_LEVEL, "вещь не может быть найдена по id = null");
        Item item = repository.findById(id).orElseThrow(
                () -> new NotFoundException("вещь с id = " + id + " не найдена")
        );
        return itemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getAllItemsByUser(Long userId) {
        ValidationTool.checkId(userId, PROGRAM_LEVEL, "вещи не могут быть найдена по id_user = null");
        List<Item> items = repository.findAllByOwner(userId);
        return itemMapper.toDtoList(items);
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Поиск вещей, имя или описание которых содержат: {}.", text);

        List<Item> items = repository.searchItem(text);
        return itemMapper.toDtoList(items);
    }
}