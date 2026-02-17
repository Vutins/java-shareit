package ru.practicum.shareit.item;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.RequestCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(Long id, ItemDto itemDto, Long userId);

    ItemDto getItemById(Long id);

    ItemDto getItemByIdWithDetails(Long id, Long userId);

    List<ItemDto> getAllItemsByUser(Long userId);

    List<ItemDto> searchItem(String text, Long userId);

    CommentDto addComment(Long userId, Long itemId, RequestCommentDto requestCommentDto);
}