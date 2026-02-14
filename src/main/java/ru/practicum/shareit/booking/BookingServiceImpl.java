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
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

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
        if (requestBookingDto.getStart() == null) {
            throw new ValidationException("Дата начала бронирования должна быть указана");
        }
        if (requestBookingDto.getEnd() == null) {
            throw new ValidationException("Дата окончания бронирования должна быть указана");
        }
        if (requestBookingDto.getItem() == null) {
            throw new ValidationException("ID вещи должен быть указан");
        }

        UserDto booker;
        try {
            booker = userService.getUserById(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        } catch (Exception e) {
            throw new InternalServerException("Ошибка при проверке пользователя: " + e.getMessage());
        }

        ItemDto item;
        try {
            item = itemService.getItemById(requestBookingDto.getItem());
        } catch (NotFoundException e) {
            throw new NotFoundException("Вещь с id " + requestBookingDto.getItem() + " не найдена");
        }

        if (item.getOwner().equals(userId)) {
            throw new ValidationException("Владелец вещи не может забронировать её сам");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь с id " + requestBookingDto.getItem() + " недоступна для бронирования");
        }

        if (requestBookingDto.getStart().isAfter(requestBookingDto.getEnd())) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }

        if (requestBookingDto.getStart().equals(requestBookingDto.getEnd())) {
            throw new ValidationException("Даты начала и окончания не могут совпадать");
        }

        if (requestBookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала не может быть в прошлом");
        }

        boolean isBooked = repository.existsApprovedBookingsForItemBetweenDates(
                requestBookingDto.getItem(),
                requestBookingDto.getStart(),
                requestBookingDto.getEnd());

        if (isBooked) {
            throw new ValidationException("Вещь уже забронирована на указанные даты");
        }

        Booking booking = mapper.toEntity(requestBookingDto);
        booking.setBooker(userId);
        booking.setStatus(Status.WAITING);

        Booking savedBooking = repository.save(booking);
        log.info("Создано бронирование с ID: {} для пользователя с ID: {}", savedBooking.getId(), userId);

        BookingDto bookingDto = mapper.toDto(savedBooking);
        bookingDto.setBooker(booker);
        bookingDto.setItem(item);

        return bookingDto;
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

        UserDto user;
        try {
            user = userService.getUserById(userId);
        } catch (NotFoundException e) {
            throw new ValidationException("Пользователь с id " + userId + " не найден");
        }

        if (!ownerId.equals(userId)) {
            log.warn("Пользователь с ID {} попытался подтвердить бронирование {} вещи, которой не владеет",
                    userId, bookingId);
            throw new ValidationException("менять статус брони может только владелец вещи");
        }

        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Бронирование уже имеет статус: " + booking.getStatus());
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        Booking updatedBooking = repository.save(booking);

        BookingDto bookingDto = mapper.toDto(updatedBooking);
        try {
            bookingDto.setBooker(userService.getUserById(booking.getBooker()));
            bookingDto.setItem(item);
        } catch (Exception e) {
            log.warn("Не удалось загрузить данные для ответа: {}", e.getMessage());
        }
        return bookingDto;
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        if (userId == null) {
            throw new ValidationException("id пользователя не может равняться null");
        }
        if (bookingId == null) {
            throw new ValidationException("id бронирования не может равняться null");
        }

        UserDto user = userService.getUserById(userId);

        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("бронирование с id = " + bookingId + " не найдено"));

        ItemDto item = itemService.getItemById(booking.getItem());
        UserDto booker = userService.getUserById(booking.getBooker());

        Long ownerId = item.getOwner();

        if (!booking.getBooker().equals(userId) && !ownerId.equals(userId)) {
            throw new ValidationException("просматривать бронь может владелец вещи или автор бронирования");
        }

        BookingDto bookingDto = mapper.toDto(booking);
        bookingDto.setBooker(booker);
        bookingDto.setItem(item);

        return bookingDto;
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state, Integer from, Integer size) {
        log.info("Получение списка бронирований пользователя с ID={}, состояние: {}, с {} по {}",
                userId, state, from, size);

        UserDto user = userService.getUserById(userId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;

        switch (state.toUpperCase()) {
            case "CURRENT":
                bookings = repository.findCurrentByBookerId(userId, now, pageable);
                break;
            case "PAST":
                bookings = repository.findPastByBookerId(userId, now, pageable);
                break;
            case "FUTURE":
                bookings = repository.findFutureByBookerId(userId, now, pageable);
                break;
            case "WAITING":
            case "REJECTED":
                Status status = Status.valueOf(state.toUpperCase());
                bookings = repository.findByBookerAndStatusOrderByStartDesc(userId, status, pageable);
                break;
            case "ALL":
                bookings = repository.findByBookerOrderByStartDesc(userId, pageable);
                break;
            default:
                log.error("Неизвестное состояние бронирования: {}", state);
                throw new ValidationException("Unknown state: " + state);
        }

        List<BookingDto> bookingDtos = bookings.stream()
                .map(booking -> {
                    BookingDto dto = mapper.toDto(booking);
                    try {
                        dto.setBooker(userService.getUserById(booking.getBooker()));
                        dto.setItem(itemService.getItemById(booking.getItem()));
                    } catch (Exception e) {
                        log.warn("Не удалось загрузить данные для бронирования {}: {}",
                                booking.getId(), e.getMessage());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Найдено {} бронирований для пользователя с ID={}", bookingDtos.size(), userId);
        return bookingDtos;
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, String state, Integer from, Integer size) {
        log.info("Получение списка бронирований для вещей владельца с ID={}, состояние: {}, с {} по {}",
                ownerId, state, from, size);

        UserDto owner = userService.getUserById(ownerId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;

        switch (state.toUpperCase()) {
            case "CURRENT":
                bookings = repository.findCurrentByOwnerId(ownerId, now, pageable);
                break;
            case "PAST":
                bookings = repository.findPastByOwnerId(ownerId, now, pageable);
                break;
            case "FUTURE":
                bookings = repository.findFutureByOwnerId(ownerId, now, pageable);
                break;
            case "WAITING":
            case "REJECTED":
                Status status = Status.valueOf(state.toUpperCase());
                bookings = repository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        ownerId,
                        status.toString(),
                        pageable);
                break;
            case "ALL":
                bookings = repository.findByItemOwnerIdOrderByStartDesc(ownerId, pageable);
                break;
            default:
                log.error("Неизвестное состояние бронирования: {}", state);
                throw new ValidationException("Unknown state: " + state);
        }

        List<BookingDto> bookingDtos = bookings.stream()
                .map(booking -> {
                    BookingDto dto = mapper.toDto(booking);
                    try {
                        dto.setBooker(userService.getUserById(booking.getBooker()));
                        dto.setItem(itemService.getItemById(booking.getItem()));
                    } catch (Exception e) {
                        log.warn("Не удалось загрузить данные для бронирования {}: {}",
                                booking.getId(), e.getMessage());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Найдено {} бронирований для вещей владельца с ID={}", bookingDtos.size(), ownerId);
        return bookingDtos;
    }
}