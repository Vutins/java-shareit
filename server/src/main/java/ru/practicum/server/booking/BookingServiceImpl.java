package ru.practicum.server.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.booking.BookingDto;
import ru.practicum.dto.booking.RequestBookingDto;
import ru.practicum.dto.booking.status.Status;
import ru.practicum.dto.exception.InternalServerException;
import ru.practicum.dto.exception.NotFoundException;
import ru.practicum.dto.exception.ValidationException;
import ru.practicum.dto.item.ItemDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.server.booking.mapper.BookingMapper;
import ru.practicum.server.booking.model.Booking;
import ru.practicum.server.booking.repository.BookingRepository;
import ru.practicum.server.item.ItemService;
import ru.practicum.server.user.UserService;

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
        log.info("Создание бронирования от пользователя {}", userId);

        validateCreateRequest(requestBookingDto, userId);
        UserDto booker = getUserById(userId);
        ItemDto item = getItemById(requestBookingDto.getItem());

        if (item.getOwner().equals(userId)) {
            throw new ValidationException("Владелец вещи не может забронировать её сам");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь с id " + requestBookingDto.getItem() + " недоступна для бронирования");
        }

        checkItemAvailability(requestBookingDto);

        Booking booking = mapper.toEntity(requestBookingDto);
        booking.setBooker(userId);
        booking.setStatus(Status.WAITING);

        Booking savedBooking = repository.save(booking);
        log.info("Создано бронирование с ID: {} в статусе WAITING", savedBooking.getId());

        BookingDto bookingDto = mapper.toDto(savedBooking);
        bookingDto.setBooker(booker);
        bookingDto.setItem(item);

        return bookingDto;
    }

    @Override
    @Transactional
    public BookingDto approve(Long bookingId, Boolean approved, Long userId) {
        log.info("Подтверждение/отклонение бронирования {} от владельца {}", bookingId, userId);

        if (bookingId == null) {
            throw new ValidationException("id бронирования не может равняться null");
        }
        if (approved == null) {
            throw new ValidationException("статус подтверждения не может равняться null");
        }

        Booking booking = getBookingById(bookingId);

        ItemDto item = getItemById(booking.getItem());

        if (!item.getOwner().equals(userId)) {
            log.warn("Пользователь {} попытался подтвердить чужое бронирование {}", userId, bookingId);
            throw new ValidationException("Подтверждать бронирование может только владелец вещи");
        }

        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Бронирование уже имеет статус: " + booking.getStatus());
        }

        Status newStatus = approved ? Status.APPROVED : Status.REJECTED;
        booking.setStatus(newStatus);

        Booking updatedBooking = repository.save(booking);
        log.info("Бронирование {} переведено в статус {}", bookingId, newStatus);

        BookingDto bookingDto = mapper.toDto(updatedBooking);
        try {
            bookingDto.setBooker(getUserById(booking.getBooker()));
            bookingDto.setItem(item);
        } catch (Exception e) {
            log.warn("Не удалось загрузить данные для ответа: {}", e.getMessage());
        }

        return bookingDto;
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        log.info("Получение информации о бронировании {} пользователем {}", bookingId, userId);

        if (bookingId == null) {
            throw new ValidationException("id бронирования не может равняться null");
        }

        getUserById(userId);

        Booking booking = getBookingById(bookingId);

        ItemDto item = getItemById(booking.getItem());

        if (!booking.getBooker().equals(userId) && !item.getOwner().equals(userId)) {
            log.warn("Пользователь {} попытался получить чужое бронирование {}", userId, bookingId);
            throw new ValidationException("Просматривать бронирование может только автор или владелец вещи");
        }
        BookingDto bookingDto = mapper.toDto(booking);
        bookingDto.setBooker(getUserById(booking.getBooker()));
        bookingDto.setItem(item);

        return bookingDto;
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state, Integer from, Integer size) {
        log.info("Получение списка бронирований пользователя {}, состояние: {}", userId, state);

        getUserById(userId);

        validatePagination(from, size);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = getBookingsByState(userId, state, now, pageable, false);

        return convertToDtoList(bookings);
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, String state, Integer from, Integer size) {
        log.info("Получение списка бронирований для вещей владельца {}, состояние: {}", ownerId, state);

        getUserById(ownerId);

        validatePagination(from, size);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = getBookingsByState(ownerId, state, now, pageable, true);

        return convertToDtoList(bookings);
    }

    private List<Booking> getBookingsByState(Long userId, String state, LocalDateTime now,
                                             Pageable pageable, boolean isOwner) {
        String upperState = state.toUpperCase();

        if (isOwner) {
            switch (upperState) {
                case "ALL":
                    return repository.findByItemOwnerIdOrderByStartDesc(userId, pageable);
                case "CURRENT":
                    return repository.findCurrentByOwnerId(userId, now, pageable);
                case "PAST":
                    return repository.findPastByOwnerId(userId, now, pageable);
                case "FUTURE":
                    return repository.findFutureByOwnerId(userId, now, pageable);
                case "WAITING":
                    return repository.findByItemOwnerIdAndStatusOrderByStartDesc(
                            userId, Status.WAITING.toString(), pageable);
                case "REJECTED":
                    return repository.findByItemOwnerIdAndStatusOrderByStartDesc(
                            userId, Status.REJECTED.toString(), pageable);
                default:
                    log.error("Неизвестное состояние бронирования: {}", state);
                    throw new ValidationException("Unknown state: " + state);
            }
        } else {
            switch (upperState) {
                case "ALL":
                    return repository.findByBookerOrderByStartDesc(userId, pageable);
                case "CURRENT":
                    return repository.findCurrentByBookerId(userId, now, pageable);
                case "PAST":
                    return repository.findPastByBookerId(userId, now, pageable);
                case "FUTURE":
                    return repository.findFutureByBookerId(userId, now, pageable);
                case "WAITING":
                    return repository.findByBookerAndStatusOrderByStartDesc(userId, Status.WAITING, pageable);
                case "REJECTED":
                    return repository.findByBookerAndStatusOrderByStartDesc(userId, Status.REJECTED, pageable);
                default:
                    log.error("Неизвестное состояние бронирования: {}", state);
                    throw new ValidationException("Unknown state: " + state);
            }
        }
    }

    private void validateCreateRequest(RequestBookingDto requestBookingDto, Long userId) {
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

        LocalDateTime now = LocalDateTime.now();

        if (requestBookingDto.getStart().isAfter(requestBookingDto.getEnd())) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }
        if (requestBookingDto.getStart().equals(requestBookingDto.getEnd())) {
            throw new ValidationException("Даты начала и окончания не могут совпадать");
        }
        if (requestBookingDto.getStart().isBefore(now)) {
            throw new ValidationException("Дата начала не может быть в прошлом");
        }
        if (requestBookingDto.getEnd().isBefore(now)) {
            throw new ValidationException("Дата окончания не может быть в прошлом");
        }
    }

    private void checkItemAvailability(RequestBookingDto requestBookingDto) {
        boolean isBooked = repository.existsApprovedBookingsForItemBetweenDates(
                requestBookingDto.getItem(),
                requestBookingDto.getStart(),
                requestBookingDto.getEnd());

        if (isBooked) {
            throw new ValidationException("Вещь уже забронирована на указанные даты");
        }
    }

    private void validatePagination(Integer from, Integer size) {
        if (from < 0) {
            throw new ValidationException("Параметр 'from' не может быть отрицательным");
        }
        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным");
        }
    }

    private UserDto getUserById(Long userId) {
        try {
            return userService.getUserById(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        } catch (Exception e) {
            throw new InternalServerException("Ошибка при проверке пользователя: " + e.getMessage());
        }
    }

    private ItemDto getItemById(Long itemId) {
        try {
            return itemService.getItemById(itemId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Вещь с id " + itemId + " не найдена");
        }
    }

    private Booking getBookingById(Long bookingId) {
        return repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("бронирование с id = " + bookingId + " не найдено"));
    }

    private List<BookingDto> convertToDtoList(List<Booking> bookings) {
        return bookings.stream()
                .map(booking -> {
                    BookingDto dto = mapper.toDto(booking);
                    try {
                        dto.setBooker(getUserById(booking.getBooker()));
                        dto.setItem(getItemById(booking.getItem()));
                    } catch (Exception e) {
                        log.warn("Не удалось загрузить данные для бронирования {}: {}",
                                booking.getId(), e.getMessage());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
}