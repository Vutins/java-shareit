package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.validation.ValidationTool;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper userMapper;
    private static final String PROGRAM_LEVEL = "UserService";

    @Override
    @Transactional
    public UserDto create(UserDto user) {
        if (user.getEmail() == null) {
            throw new InternalServerException("для создания пользователя укажите email");
        }
        findByEmail(user.getEmail()).ifPresent(existingUser -> {
            throw new InternalServerException("Пользователь с email " + user.getEmail() + " уже существует");
        });
        User userCreate = userMapper.toEntity(user);
        return userMapper.toDto(repository.save(userCreate));
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        ValidationTool.checkId(id, PROGRAM_LEVEL, "User не может быть обновлен по id = null");

        User existingUser = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с ID %d не найден", id)
                ));
        ValidationTool.userCheck(existingUser, PROGRAM_LEVEL);

        String newEmail = userDto.getEmail() != null ? userDto.getEmail() : existingUser.getEmail();

        if (userDto.getEmail() != null && !existingUser.getEmail().equals(newEmail)) {
            repository.findByEmail(newEmail).ifPresent(otherUser -> {
                if (!otherUser.getId().equals(id)) {
                    throw new InternalServerException("Email " + newEmail + " уже используется другим пользователем");
                }
            });
        }

        userMapper.updateUserFromDto(userDto, existingUser);
        existingUser.setId(id);

        repository.save(existingUser);
        return userMapper.toDto(existingUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        try {
            User user = repository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Пользователь с ID {} не найден в БД", id);
                        return new NotFoundException(
                                String.format("Пользователь с ID %d не найден", id)
                        );
                    });
            log.info("{}: Пользователь с ID {} найден", PROGRAM_LEVEL, id);
            return userMapper.toDto(user);
        } catch (NotFoundException e) {
            log.warn("NotFoundException из UserServiceImpl: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("пользователя с id = " + id + "не найден");
        }
        repository.deleteById(id);
    }

    private Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }
}
