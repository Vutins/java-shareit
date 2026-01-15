package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

public interface UserStorage {

    User create(User user);

    User update(Long id, User user);

    User getUserById(Long id);

    boolean deleteUserById(Long id);
}
