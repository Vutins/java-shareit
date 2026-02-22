package ru.practicum.server.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.server.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwner(Long userId);

    @Query("SELECT i FROM Item i " +
            "WHERE (UPPER(i.name) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "OR UPPER(i.description) LIKE UPPER(CONCAT('%', :text, '%'))) " +
            "AND i.available = true")
    List<Item> searchItem(@Param("text") String text);

    @Query("SELECT i FROM Item i WHERE i.request = :requestId")
    List<Item> findByRequestId(@Param("requestId") Long requestId);
}
