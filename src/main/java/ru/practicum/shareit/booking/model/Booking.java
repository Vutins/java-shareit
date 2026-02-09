package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.status.Status;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bookings")
public class Booking {

    @Id
    @NotNull
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @PastOrPresent
    @Column(name = "start")
    private LocalDateTime start;
    @PastOrPresent
    @Column(name = "ended")
    private LocalDateTime end;
    @NotNull
    @Column(name = "booker_id")
    private Long booker;
    @NotNull
    @Column(name = "item_id")
    private Long item;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;
}
