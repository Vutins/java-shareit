package ru.practicum.server.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.server.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    @Query("SELECT ir FROM ItemRequest ir WHERE ir.requestor = :userId ORDER BY ir.created DESC")
    List<ItemRequest> findByRequesterIdOrderByCreatedDesc(@Param("userId") Long userId);

    @Query("SELECT ir FROM ItemRequest ir WHERE ir.requestor <> :userId ORDER BY ir.created DESC")
    List<ItemRequest> findByRequesterIdNotOrderByCreatedDesc(@Param("userId") Long userId, Pageable pageable);
}