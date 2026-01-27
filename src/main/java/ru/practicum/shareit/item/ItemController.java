package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemServiceImpl itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ItemDto create(@Valid @RequestBody ItemDto itemDto,
                          @Valid @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("запрос на создание вещи");
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDto update(@PathVariable Long id,
                          @Valid @RequestBody ItemDto itemDto,
                           @Valid @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("запрос на обновление вещи");
        return itemService.update(id, itemDto, userId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDto getItemById(@PathVariable Long id) {
        log.info("запрос на вывод вещи");
        return itemService.getItemById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> getAllItemsByUser(@Valid @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("запрос на получение вещей пользователя");
        return itemService.getAllItemsByUser(userId);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> searchItem(@RequestParam String text) {
        log.info("запрос на поиск вещей");
        return itemService.searchItem(text);
    }
}