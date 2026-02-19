package ru.practicum.server.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.RequestCommentDto;
import ru.practicum.dto.item.ItemDto;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> create(@Valid @RequestBody ItemDto itemDto,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("=== СОЗДАНИЕ ВЕЩИ ===");
        log.info("UserId: {}", userId);
        log.info("Полученный ItemDto: {}", itemDto);
        log.info("Поле name: {}", itemDto.getName());
        log.info("Поле description: {}", itemDto.getDescription());
        log.info("Поле available: {}", itemDto.getAvailable());
        log.info("Поле request (маппится из requestId): {}", itemDto.getRequest());

        ItemDto createdItem = itemService.create(itemDto, userId);
        return ResponseEntity
                .created(URI.create("/items/" + createdItem.getId()))
                .body(createdItem);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemDto> update(@PathVariable Long id,
                                          @Valid @RequestBody ItemDto itemDto,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("запрос на обновление вещи {} от пользователя {}", id, userId);
        ItemDto updatedItem = itemService.update(id, itemDto, userId);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable Long id,
                                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("запрос на вывод вещи {} от пользователя {}", id, userId);
        ItemDto item = itemService.getItemByIdWithDetails(id, userId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItemsByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("запрос на получение вещей пользователя {}", userId);
        List<ItemDto> items = itemService.getAllItemsByUser(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItem(@RequestParam String text,
                                                    @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("запрос на поиск вещей по тексту '{}' от пользователя {}", text, userId);
        List<ItemDto> items = itemService.searchItem(text, userId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody RequestCommentDto requestCommentDto) {
        log.info("запрос на добавление комментария к вещи {} от пользователя {}", itemId, userId);
        CommentDto comment = itemService.addComment(userId, itemId, requestCommentDto);
        return ResponseEntity.ok(comment);
    }
}