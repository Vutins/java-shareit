package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestBookingDto {

    private Long id;
    @NotNull
    @PastOrPresent(message = "дата бронирования должна быть в настоящем или в прошедшем")
    private LocalDateTime start;
    @NotNull
    @Future(message = "дата окончания должна быть в будущем")
    private LocalDateTime end;
    @NotNull
    @Positive(message = "ID не может быть отрицательным")
    private Long item;
}
