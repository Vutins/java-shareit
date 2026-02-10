package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.RequestBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository repository;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingMapper mapper;

    @Override
    @Transactional
    public BookingDto create(RequestBookingDto requestBookingDto, Long userId) {
        if (userId == null) {
            throw new ValidationException("id пользователя не может равняться null");
        }
        if (requestBookingDto == null) {
            throw new ValidationException("запрос на бронь не может равняться null");
        }
        userService.getUserById(userId);
        itemService.getItemById(requestBookingDto.getItem());

        Booking booking = mapper.toEntity(requestBookingDto);
        booking.setBooker(userId);

        repository.save(booking);
        return mapper.toDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approve(Long bookingId, Boolean approved, Long userId) {
        if (userId == null) {
            throw new ValidationException("id пользователя не может равняться null");
        }
        if (bookingId == null) {
            throw new ValidationException("id бронирования не может равняться null");
        }
        if (approved == null) {
            throw new ValidationException("статус бронирования не может равняться null");
        }
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("бронь не найдена по id = " + bookingId));
        ItemDto item = itemService.getItemById(booking.getItem());
        Long ownerId = item.getOwner();
        if (!ownerId.equals(userId)) {
            throw new ValidationException("менять статус брони может только владелец вещи");
        }
        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Бронирование уже имеет статус: " + booking.getStatus());
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        repository.save(booking);
        return mapper.toDto(booking);
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        if (userId == null) {
            throw new ValidationException("id пользователя не может равняться null");
        }
        if (bookingId == null) {
            throw new ValidationException("id бронирования не может равняться null");
        }

        userService.getUserById(userId);
        Booking booking = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("бронирование с id = " + bookingId +" не найдено"));

        ItemDto item = itemService.getItemById(booking.getItem());
        Long ownerId = item.getOwner();
        if (!booking.getBooker().equals(userId) || !ownerId.equals(userId)) {
            throw new ValidationException("просматривать бронь может владелец вещи или автор бронирования");
        }
        return mapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state, Integer from, Integer size) {
        log.info("Получение списка бронирований пользователя с ID={}, состояние: {}, с {} по {}",
                userId, state, from, size);
        userService.getUserById(userId);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();
        List<BookingDto> bookings;
        switch (state.toUpperCase()) {
            case "CURRENT":
                bookings = repository.findCurrentByBookerId(userId, now, pageable).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList());
                break;
            case "PAST":
                bookings = repository.findPastByBookerId(userId, now, pageable).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList());
                break;
            case "FUTURE":
                bookings = repository.findFutureByBookerId(userId, now, pageable).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList());
                break;
            case "WAITING":
            case "REJECTED":
                Status status = Status.valueOf(state.toUpperCase());
                bookings = repository.findByBookerIdAndStatusOrderByStartDesc(userId, status, pageable).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList());
                break;
            case "ALL":
                bookings = repository.findByBookerIdOrderByStartDesc(userId, pageable).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList());
                break;
            default:
                log.error("Неизвестное состояние бронирования: {}", state);
                throw new ValidationException("Unknown state: " + state);
        }
        log.info("Найдено {} бронирований для пользователя с ID={}", bookings.size(), userId);
        return bookings;
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, String state, Integer from, Integer size) {
        log.info("Получение списка бронирований для вещей владельца с ID={}, состояние: {}, с {} по {}",
                ownerId, state, from, size);
        userService.getUserById(ownerId);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();
        List<BookingDto> bookings;
        switch (state.toUpperCase()) {
            case "CURRENT":
                bookings = repository.findCurrentByOwnerId(ownerId, now, pageable).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList());
                break;
            case "PAST":
                bookings = repository.findPastByOwnerId(ownerId, now, pageable).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList());
                break;
            case "FUTURE":
                bookings = repository.findFutureByOwnerId(ownerId, now, pageable).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList());
                break;
            case "WAITING":
            case "REJECTED":
                Status status = Status.valueOf(state.toUpperCase());
                bookings = repository.findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, status, pageable).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList());
                break;
            case "ALL":
                bookings = repository.findByItemOwnerIdOrderByStartDesc(ownerId, pageable).stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList());
                break;
            default:
                log.error("Неизвестное состояние бронирования: {}", state);
                throw new ValidationException("Unknown state: " + state);
        }
        log.info("Найдено {} бронирований для вещей владельца с ID={}", bookings.size(), ownerId);
        return bookings;
    }
}
