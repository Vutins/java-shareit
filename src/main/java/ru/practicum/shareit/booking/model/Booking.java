package ru.practicum.shareit.booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Column(name = "status_id")
    private String status;
}
