package ru.practicum.shareit.booking.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

    @NotNull
    private Long id;
    @NotNull
    @PastOrPresent
    private LocalDateTime start;
    @PastOrPresent
    private LocalDateTime end;
    @NotNull
    private Long item;
    @NotNull
    private Long booker;
    @NotNull
    private String status;
}
