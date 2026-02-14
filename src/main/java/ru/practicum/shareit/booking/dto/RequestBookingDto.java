package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestBookingDto {

    @JsonProperty("itemId")
    @NotNull(message = "ID вещи должен быть указан")
    @Positive(message = "ID вещи должен быть положительным числом")
    Long item;

    @NotNull(message = "Дата начала бронирования должна быть указана")
    @Future(message = "Дата начала должна быть в будущем")
    LocalDateTime start;

    @NotNull(message = "Дата окончания бронирования должна быть указана")
    @Future(message = "Дата окончания должна быть в будущем")
    LocalDateTime end;
}
