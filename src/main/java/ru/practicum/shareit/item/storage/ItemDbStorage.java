package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemRowMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserRowMapper;
import ru.practicum.shareit.user.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemDbStorage implements ItemStorage {

    private final JdbcOperations jdbc;
    private final ItemRowMapper itemMapper;
    private final UserRowMapper userMapper;

    @Override
    public Item create(Item item, Long user_id) {
        final String CREATE_ITEM_QUERY = """
            INSERT INTO items (name, description, available, owner_id)
            VALUES (?, ?, ?, ?);
        """;

        final String GET_USER_CHECK = """
                SELECT *
                FROM users
                WHERE id = ?
                """;

        List<User> userCheck = jdbc.query(GET_USER_CHECK, userMapper, user_id);
        if (userCheck.isEmpty()) {
            throw new NotFoundException("user с таким id не существует");
        }

        final Object[] params = {
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                user_id
        };

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        int affectedRows = jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(CREATE_ITEM_QUERY, Statement.RETURN_GENERATED_KEYS);

            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKeyAs(Long.class);
        if (generatedId == null) {
            throw new InternalServerException("ItemDbStorage: Не удалось сохранить данные Item");
        }

        return new Item (
                generatedId,
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                user_id,
                item.getRequest()
        );
    }

    @Override
    public Item update(Long id, Item item, Long user_id) {
        final String UPDATE_ITEM_QUERY = """
            UPDATE items SET name = ?, description = ?, available = ?, owner_id = ?
            WHERE id = ?;
        """;

        final String FIND_ITEM_BY_ID_QUERY = """
            SELECT *
            FROM items
            WHERE id = ?;
        """;

        final Object[] params = {
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                user_id,
                id
        };
        Item itemCheck = jdbc.queryForObject(FIND_ITEM_BY_ID_QUERY, itemMapper, id);

        if (itemCheck.getOwner().equals(user_id)) {
                int rowsUpdate = jdbc.update(UPDATE_ITEM_QUERY, params);
                if (rowsUpdate == 0) {
                    throw new InternalServerException("ItemDbStorage: Не удалось обновить данные Item");
                }
        } else {
            throw new NotFoundException("нельзя обновить вещь с user_id отличным от id владельца");
        }

        Item itemUpdate;
        try {
            itemUpdate = jdbc.queryForObject(FIND_ITEM_BY_ID_QUERY, itemMapper, id);
        } catch (EmptyResultDataAccessException ignored) {
            log.warn("ItemDbStorage: Не удалось получить объект Item по его ID - не найден в приложении");
            throw new NotFoundException("ItemDbStorage: Item c ID: " + id + " не найден в приложении");
        }
        return itemUpdate;
    }

    @Override
    public Item getItemById(Long id) {
        final String FIND_ITEM_BY_ID_QUERY = """
            SELECT *
            FROM items
            WHERE id = ?;
        """;

        Item item;
        try {
            item = jdbc.queryForObject(FIND_ITEM_BY_ID_QUERY, itemMapper, id);
        } catch (EmptyResultDataAccessException ignored) {
            log.warn("ItemDbStorage: Не удалось получить объект Item по его ID - не найден в приложении");
            throw new NotFoundException("ItemDbStorage: Item c ID: " + id + " не найден в приложении");
        }
        return item;
    }

    @Override
    public List<Item> getAllItemsByUser(Long userId) {
        final String FIND_ALL_ITEMS_BY_OWNER_ID = """
                SELECT *
                FROM items
                WHERE owner_id = ?;
        """;

        List<Item> items;
        try {
            items = jdbc.query(FIND_ALL_ITEMS_BY_OWNER_ID, itemMapper, userId);
            return items != null ? items : Collections.emptyList();
        } catch (Exception e) {
            log.error("Ошибка при получении всех вещей пользователя с ID: {}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Item> searchItem(String text) {
        String searchPattern = "%" + text.trim() + "%";

        String LIST_ITEM_QUERY = """
            SELECT i.*
            FROM items i
            WHERE i.available = true
            AND (LOWER(i.name) LIKE LOWER(?)
                 OR LOWER(i.description) LIKE LOWER(?))
            ORDER BY i.id
        """;

        List<Item> itemSearch;
        try {
            itemSearch = jdbc.query(LIST_ITEM_QUERY, itemMapper, searchPattern, searchPattern);
        } catch (Exception e) {
            log.error("Ошибка при получении всех вещей при поиске");
            return Collections.emptyList();
        }
        return itemSearch;
    }
}
