package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ItemDto> create(@Valid @RequestBody ItemDto itemDto,
                                          @Valid @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("запрос на создание вещи");
        return ResponseEntity.ok().body(itemService.create(itemDto, userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemDto> update(@PathVariable Long id,
                          @Valid @RequestBody ItemDto itemDto,
                           @Valid @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("запрос на обновление вещи");
        return ResponseEntity.ok(itemService.update(id, itemDto, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable Long id) {
        log.info("запрос на вывод вещи");
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItemsByUser(@Valid @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("запрос на получение вещей пользователя");
        return ResponseEntity.ok( itemService.getAllItemsByUser(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItem(@RequestParam String text) {
        log.info("запрос на поиск вещей");
        return ResponseEntity.ok(itemService.searchItem(text));
    }
}