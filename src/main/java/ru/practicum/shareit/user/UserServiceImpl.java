package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserInMemoryStorage;
import ru.practicum.shareit.validation.ValidationTool;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserInMemoryStorage userStorage;
    private final UserMapper userMapper;
    private static final String PROGRAM_LEVEL = "UserService";

    @Override
    public UserDto create(UserDto user) {
        if (user.getEmail() == null) {
            throw new InternalServerException("для создания пользователя укажите email");
        }
        findByEmail(user.getEmail()).ifPresent(existingUser -> {
            throw new InternalServerException("Пользователь с email " + user.getEmail() + " уже существует");
        });
        User userCreate = userMapper.toEntity(user);
        return userMapper.toDto(userStorage.create(userCreate));
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        ValidationTool.checkId(id, PROGRAM_LEVEL, "User не может быть обновлен по id = null");

        User existingUser = userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с ID %d не найден", id)
                ));
        ValidationTool.userCheck(existingUser, PROGRAM_LEVEL);

        String newEmail = userDto.getEmail() != null ? userDto.getEmail() : existingUser.getEmail();

        if (userDto.getEmail() != null && !existingUser.getEmail().equals(newEmail)) {
            userStorage.findByEmail(newEmail).ifPresent(otherUser -> {
                if (!otherUser.getId().equals(id)) {
                    throw new InternalServerException("Email " + newEmail + " уже используется другим пользователем");
                }
            });
        }

        userMapper.updateUserFromDto(userDto, existingUser);
        existingUser.setId(id);

        userStorage.update(id, existingUser);
        return userMapper.toDto(existingUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        ValidationTool.checkId(id, PROGRAM_LEVEL, "Id не может равняться null");
        User user = userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с ID %d не найден", id)
                ));

        log.info("{}: Пользователь с ID {} найден", PROGRAM_LEVEL, id);
        return userMapper.toDto(user);
    }

    @Override
    public void deleteUserById(Long id) {
        String message;

        if (id == null || id < 1) {
            message = String.format(
                    "%s : Попытка удалить user по ID = %s",
                    PROGRAM_LEVEL, String.valueOf(id)
            );
            log.warn(message);
            throw new ValidationException(message);
        }

        if (userStorage.deleteUserById(id) == false) {
            message = String.format(
                    "%s : User с ID = %s не найден в приложении",
                    PROGRAM_LEVEL, String.valueOf(id));
            log.warn(message);
            throw new NotFoundException(message);
        }

        message = String.format(
                "%s : User ID %s успешно удален",
                PROGRAM_LEVEL, String.valueOf(id));
        log.info(message);
    }

    private Optional<User> findByEmail(String email) {
        return userStorage.findByEmail(email);
    }
}
