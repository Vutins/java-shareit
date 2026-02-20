package ru.practicum.dto.booking;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.booking.status.Status;
import ru.practicum.dto.item.ItemDto;
import ru.practicum.dto.user.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDto {

    Long id;
    LocalDateTime start;
    LocalDateTime end;
    UserDto booker;
    ItemDto item;
    Status status;
}
