package ru.practicum.server.user;

import ru.practicum.dto.user.UserDto;

public interface UserService {

    UserDto create(UserDto user);

    UserDto update(Long id, UserDto userDto);

    UserDto getUserById(Long id);

    void deleteUserById(Long id);
}
