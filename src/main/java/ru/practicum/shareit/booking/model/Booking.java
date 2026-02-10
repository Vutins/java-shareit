package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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
    @NotNull
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @NotNull
    @PastOrPresent
    @Column(name = "start")
    LocalDateTime start;
    @PastOrPresent
    @Column(name = "ended")
    LocalDateTime end;
    @NotNull
    @Column(name = "booker_id")
    Long booker;
    @NotNull
    @Column(name = "item_id")
    Long item;
    @NotNull
    @Enumerated(EnumType.STRING)
    Status status;
}
