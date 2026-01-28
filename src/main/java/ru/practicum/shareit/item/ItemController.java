package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemServiceImpl itemService;

    @PostMapping
    public ResponseEntity<ItemDto> create(@Valid @RequestBody ItemDto itemDto,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("запрос на создание вещи");
        ItemDto createdItem = itemService.create(itemDto, userId);
        return ResponseEntity
                .created(URI.create("/items/" + createdItem.getId()))
                .body(createdItem);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemDto> update(@PathVariable Long id,
                                          @Valid @RequestBody ItemDto itemDto,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("запрос на обновление вещи");
        ItemDto updatedItem = itemService.update(id, itemDto, userId);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable Long id) {
        log.info("запрос на вывод вещи");
        ItemDto item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItemsByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("запрос на получение вещей пользователя");
        List<ItemDto> items = itemService.getAllItemsByUser(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItem(@RequestParam String text) {
        log.info("запрос на поиск вещей");
        List<ItemDto> items = itemService.searchItem(text);
        return ResponseEntity.ok(items);
    }
}