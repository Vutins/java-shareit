package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerOrderByStartDesc(Long booker, Pageable pageable);

    List<Booking> findByBookerAndStatusOrderByStartDesc(Long booker, Status status, Pageable pageable);

    @Query(value = "SELECT * FROM bookings WHERE booker_id = :userId " +
            "AND start <= :now AND ended >= :now " +
            "ORDER BY start DESC",
            nativeQuery = true)
    List<Booking> findCurrentByBookerId(@Param("userId") Long userId,
                                        @Param("now") LocalDateTime now,
                                        Pageable pageable);

    @Query(value = "SELECT * FROM bookings WHERE booker_id = :userId " +
            "AND ended < :now " +
            "ORDER BY start DESC",
            nativeQuery = true)
    List<Booking> findPastByBookerId(@Param("userId") Long userId,
                                     @Param("now") LocalDateTime now,
                                     Pageable pageable);

    @Query(value = "SELECT * FROM bookings WHERE booker_id = :userId " +
            "AND start > :now " +
            "ORDER BY start DESC",
            nativeQuery = true)
    List<Booking> findFutureByBookerId(@Param("userId") Long userId,
                                       @Param("now") LocalDateTime now,
                                       Pageable pageable);

    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN items i ON b.item_id = i.id " +
            "WHERE i.owner_id = :ownerId " +
            "ORDER BY b.start DESC",
            nativeQuery = true)
    List<Booking> findByItemOwnerIdOrderByStartDesc(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN items i ON b.item_id = i.id " +
            "WHERE i.owner_id = :ownerId " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC",
            nativeQuery = true)
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(@Param("ownerId") Long ownerId,
                                                             @Param("status") String status,
                                                             Pageable pageable);

    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN items i ON b.item_id = i.id " +
            "WHERE i.owner_id = :ownerId " +
            "AND b.start <= :now AND b.ended >= :now " +
            "ORDER BY b.start DESC",
            nativeQuery = true)
    List<Booking> findCurrentByOwnerId(@Param("ownerId") Long ownerId,
                                       @Param("now") LocalDateTime now,
                                       Pageable pageable);

    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN items i ON b.item_id = i.id " +
            "WHERE i.owner_id = :ownerId " +
            "AND b.ended < :now " +
            "ORDER BY b.start DESC",
            nativeQuery = true)
    List<Booking> findPastByOwnerId(@Param("ownerId") Long ownerId,
                                    @Param("now") LocalDateTime now,
                                    Pageable pageable);

    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN items i ON b.item_id = i.id " +
            "WHERE i.owner_id = :ownerId " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC",
            nativeQuery = true)
    List<Booking> findFutureByOwnerId(@Param("ownerId") Long ownerId,
                                      @Param("now") LocalDateTime now,
                                      Pageable pageable);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM bookings WHERE item_id = :itemId " +
            "AND status = 'APPROVED' " +
            "AND (:start BETWEEN start AND ended OR " +
            ":end BETWEEN start AND ended OR " +
            "(start <= :start AND ended >= :end))",
            nativeQuery = true)
    boolean existsApprovedBookingsForItemBetweenDates(
            @Param("itemId") Long itemId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = "SELECT * FROM bookings WHERE item_id = :itemId " +
            "AND booker_id = :userId " +
            "AND status = 'APPROVED' " +
            "AND ended < :now " +
            "ORDER BY ended DESC",
            nativeQuery = true)
    List<Booking> findLastBookingForItem(
            @Param("itemId") Long itemId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Query(value = "SELECT * FROM bookings WHERE item_id = :itemId " +
            "AND status = 'APPROVED' " +
            "AND start > :now " +
            "ORDER BY start ASC",
            nativeQuery = true)
    List<Booking> findNextBookingForItem(
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM bookings WHERE booker_id = :bookerId " +
            "AND item_id = :itemId " +
            "AND ended < :endDate",
            nativeQuery = true)
    boolean existsByBookerIdAndItemIdAndEndIsBefore(
            @Param("bookerId") Long bookerId,
            @Param("itemId") Long itemId,
            @Param("endDate") LocalDateTime endDate);

    @Modifying
    @Query(value = "DELETE FROM bookings WHERE booker_id = :userId", nativeQuery = true)
    void deleteByBookerId(@Param("userId") Long userId);

    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN items i ON i.id = b.item_id " +
            "WHERE b.booker_id = :userId " +
            "AND i.id = :itemId " +
            "AND b.status = 'APPROVED'",
            nativeQuery = true)
    List<Booking> findAllByUserBookings(@Param("userId") Long userId,
                                        @Param("itemId") Long itemId);
}