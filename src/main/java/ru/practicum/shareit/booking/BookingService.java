package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.RequestBookingDto;

import java.util.List;

public interface BookingService {

    BookingDto create(RequestBookingDto requestBookingDto, Long userId);

    BookingDto approve(Long bookingId, Boolean approved, Long userId);

    BookingDto getById(Long bookingId, Long userId);

    List<BookingDto> getUserBookings(Long userId, String state, Integer from, Integer size);

    List<BookingDto> getOwnerBookings(Long ownerId, String state, Integer from, Integer size);
}
