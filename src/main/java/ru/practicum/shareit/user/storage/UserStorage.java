package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.Optional;

public interface UserStorage {

    User create(User user);

    User update(Long id, User user);

    Optional<User> getUserById(Long id);

    boolean deleteUserById(Long id);

    Optional<User> findByEmail(String email);
}
