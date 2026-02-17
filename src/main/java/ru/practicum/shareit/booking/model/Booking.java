package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.status.Status;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bookings")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @NotNull
    @FutureOrPresent(message = "Дата начала должна быть в настоящем или будущем")
    @Column(name = "start", nullable = false)
    LocalDateTime start;

    @NotNull
    @Future(message = "Дата окончания должна быть в будущем")
    @Column(name = "ended", nullable = false)
    LocalDateTime end;

    @NotNull
    @Column(name = "booker_id", nullable = false)
    Long booker;

    @NotNull
    @Column(name = "item_id", nullable = false)
    Long item;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Status status;
}