package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.mapper.UserRowMapper;
import ru.practicum.shareit.user.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Slf4j
@Repository
public class UserDbStorage implements UserStorage {

    private final JdbcOperations jdbc;
    private final UserRowMapper mapper;

    @Autowired
    public UserDbStorage(final JdbcOperations jdbc, final UserRowMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public User create(User user) {
        final String CREATE_USER_QUERY = """
            INSERT INTO users (name, email)
            VALUES (?, ?);
        """;

        final Object[] params = {
                user.getName(),
                user.getEmail()
        };

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        int affectedRows = jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(CREATE_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKeyAs(Long.class);
        if (generatedId == null) {
            throw new InternalServerException("UserDbStorage: Не удалось сохранить данные User");
        }

        return new User(
                generatedId,
                user.getName(),
                user.getEmail()
        );
    }

    @Override
    public User update(Long id, User user) {
        final String UPDATE_USER_QUERY = """
            UPDATE users SET name = ?, email = ?
            WHERE id = ?;
        """;

        final Object[] params = {
                user.getName(),
                user.getEmail(),
                id
        };

        int rowsUpdate = jdbc.update(UPDATE_USER_QUERY, params);
        if (rowsUpdate == 0) {
            throw  new InternalServerException("BaseDbStorage: Не удалось обновить данные User");
        }

        final String FIND_USER_BY_ID_QUERY = """
            SELECT *
            FROM users
            WHERE id = ?;
        """;

        User userUpdate;
        try {
            userUpdate = jdbc.queryForObject(FIND_USER_BY_ID_QUERY, mapper, id);
        } catch (EmptyResultDataAccessException ignored) {
            log.warn("UserDbStorage: Не удалось получить объект User по его ID - не найден в приложении");
            throw new NotFoundException("UserDbStorage: User c ID: " + id + " не найден в приложении");
        }
        return userUpdate;
    }

    @Override
    public User getUserById(Long id) {
        final String FIND_USER_BY_ID_QUERY = """
            SELECT *
            FROM users
            WHERE id = ?;
        """;

        User user;
        try {
            user = jdbc.queryForObject(FIND_USER_BY_ID_QUERY, mapper, id);
        } catch (EmptyResultDataAccessException ignored) {
            log.warn("UserDbStorage: Не удалось получить объект User по его ID - не найден в приложении");
            throw new NotFoundException("UserDbStorage: User c ID: " + id + " не найден в приложении");
        }
        return user;
    }

    @Override
    public boolean deleteUserById(Long id) {
        final String DELETE_USER_QUERY = """
            DELETE FROM users
            WHERE id = ?;
        """;
        return jdbc.update(DELETE_USER_QUERY, id) > 0;
    }
}
