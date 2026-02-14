package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.RequestCommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.validation.ValidationTool;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final BookingMapper bookingMapper;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private static final String PROGRAM_LEVEL = "ItemService";

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long userId) {
        ValidationTool.checkId(userId, PROGRAM_LEVEL, "при создании вещи user_id не должен равняться null");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("не найден владелец вещи по id = " + userId));

        validateItemFields(itemDto);

        Item item = itemMapper.toEntity(itemDto);
        item.setOwner(userId);

        Item savedItem = repository.save(item);
        log.info("Создана вещь с ID: {} для пользователя с ID: {}", savedItem.getId(), userId);

        ItemDto savedItemDto = itemMapper.toDto(savedItem);
        savedItemDto.setComments(Collections.emptyList());

        return savedItemDto;
    }

    @Override
    @Transactional
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

        Item updatedItem = repository.save(existingItem);

        ItemDto updatedItemDto = itemMapper.toDto(updatedItem);
        updatedItemDto.setComments(getCommentsForItem(id));

        return updatedItemDto;
    }

    @Override
    public ItemDto getItemById(Long id) {
        ValidationTool.checkId(id, PROGRAM_LEVEL, "вещь не может быть найдена по id = null");

        Item item = repository.findById(id).orElseThrow(
                () -> new NotFoundException("вещь с id = " + id + " не найдена")
        );

        ItemDto itemDto = itemMapper.toDto(item);
        itemDto.setComments(getCommentsForItem(id));

        return itemDto;
    }

    @Override
    public ItemDto getItemByIdWithDetails(Long id, Long userId) {
        ValidationTool.checkId(id, PROGRAM_LEVEL, "вещь не может быть найдена по id = null");
        ValidationTool.checkId(userId, PROGRAM_LEVEL, "пользователь не может быть null");

        Item item = repository.findById(id).orElseThrow(
                () -> new NotFoundException("вещь с id = " + id + " не найдена")
        );

        LocalDateTime now = LocalDateTime.now();
        BookingDto lastBooking = null;
        BookingDto nextBooking = null;

        if (item.getOwner().equals(userId)) {
            lastBooking = bookingRepository
                    .findLastBookingForItem(id, userId, now, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .map(bookingMapper::toDto)
                    .orElse(null);

            nextBooking = bookingRepository
                    .findNextBookingForItem(id, now, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .map(bookingMapper::toDto)
                    .orElse(null);
        }

        List<CommentDto> comments = getCommentsForItem(id);

        return itemMapper.toDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemDto> getAllItemsByUser(Long userId) {
        ValidationTool.checkId(userId, PROGRAM_LEVEL, "вещи не могут быть найдена по id_user = null");

        List<Item> items = repository.findAllByOwner(userId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    BookingDto lastBooking = bookingRepository
                            .findLastBookingForItem(item.getId(), userId, now, PageRequest.of(0, 1))
                            .stream()
                            .findFirst()
                            .map(bookingMapper::toDto)
                            .orElse(null);

                    BookingDto nextBooking = bookingRepository
                            .findNextBookingForItem(item.getId(), now, PageRequest.of(0, 1))
                            .stream()
                            .findFirst()
                            .map(bookingMapper::toDto)
                            .orElse(null);

                    List<CommentDto> comments = getCommentsForItem(item.getId());

                    return itemMapper.toDto(item, lastBooking, nextBooking, comments);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItem(String text, Long userId) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Поиск вещей по тексту: '{}' для пользователя {}", text, userId);

        List<Item> items = repository.searchItem(text);

        return items.stream()
                .map(item -> {
                    ItemDto dto = itemMapper.toDto(item);
                    dto.setComments(getCommentsForItem(item.getId())); // Добавляем комментарии
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, RequestCommentDto requestCommentDto) {
        log.info("Добавление комментария к вещи с ID={} от пользователя с ID={}", itemId, userId);

        if (requestCommentDto.getText() == null || requestCommentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        Item item = repository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        LocalDateTime now = LocalDateTime.now();

        boolean hasCompletedBooking = bookingRepository.existsByBookerIdAndItemIdAndEndIsBefore(
                userId, itemId, now);

        if (!hasCompletedBooking) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду или аренда еще не завершена");
        }

        Comment comment = Comment.builder()
                .text(requestCommentDto.getText())
                .itemId(itemId)
                .authorId(userId)
                .created(Instant.now())
                .build();

        Comment savedComment = commentRepository.save(comment);

        CommentDto savedCommentDto = commentMapper.toDto(savedComment);
        savedCommentDto.setAuthorName(author.getName());

        log.info("Добавлен комментарий: {}", savedCommentDto);
        return savedCommentDto;
    }

    private List<CommentDto> getCommentsForItem(Long itemId) {
        List<Comment> comments = commentRepository.findByItemId(itemId);

        return comments.stream()
                .map(comment -> {
                    CommentDto dto = commentMapper.toDto(comment);
                    userRepository.findById(comment.getAuthorId())
                            .ifPresent(author -> dto.setAuthorName(author.getName()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void validateItemFields(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("имя вещи не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("описание вещи не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("статус доступности вещи должен быть указан");
        }
    }
}