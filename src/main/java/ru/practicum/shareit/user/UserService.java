package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.validation.ValidationTool;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private static final String PROGRAM_LEVEL = "UserService";

    public UserDto create(UserDto user) {
        User userCreate = UserDtoMapper.toUser(user);
        return UserDtoMapper.toUserDto(userStorage.create(userCreate));
    }

    public UserDto update(Long id, UserDto userDto) {
        ValidationTool.checkId(id, PROGRAM_LEVEL, "User не может быть обновлен по id = null");

        User existingUser = userStorage.getUserById(id);
        ValidationTool.userCheck(existingUser, PROGRAM_LEVEL);

        User updatedUser = User.builder()
                .id(id)
                .name(userDto.getName() != null ? userDto.getName() : existingUser.getName())
                .email(userDto.getEmail() != null ? userDto.getEmail() : existingUser.getEmail())
                .build();

        userStorage.update(id, updatedUser);
        return UserDtoMapper.toUserDto(updatedUser);
    }

    public UserDto getUserById(Long id) {
        ValidationTool.checkId(id, PROGRAM_LEVEL, "Id не может равняться null");
        UserDto user = UserDtoMapper.toUserDto(userStorage.getUserById(id));
        log.info(PROGRAM_LEVEL, "ОБЪЕКТ НАЙДЕН");
        return user;
    }

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
}
