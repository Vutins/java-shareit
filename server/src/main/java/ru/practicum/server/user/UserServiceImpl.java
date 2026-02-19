package ru.practicum.server.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.exception.InternalServerException;
import ru.practicum.dto.exception.NotFoundException;
import ru.practicum.dto.user.UserDto;
import ru.practicum.server.user.mapper.UserMapper;
import ru.practicum.server.user.model.User;
import ru.practicum.server.user.repository.UserRepository;

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
    public UserDto create(UserDto userDto) {
        log.info("Создание пользователя с email: {}", userDto.getEmail());

        if (repository.findByEmail(userDto.getEmail()).isPresent()) {
            log.warn("Пользователь с email {} уже существует", userDto.getEmail());
            throw new InternalServerException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }

        User user = userMapper.toEntity(userDto);

        try {
            User savedUser = repository.save(user);
            log.info("Пользователь создан с id: {}", savedUser.getId());
            return userMapper.toDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при сохранении пользователя: {}", e.getMessage());
            throw new InternalServerException("Пользователь с таким email уже существует");
        }
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        log.info("Обновление пользователя с id: {}", id);

        if (userDto.getId() != null && !userDto.getId().equals(id)) {
            log.warn("ID в теле запроса {} не совпадает с ID в пути {}", userDto.getId(), id);
            throw new InternalServerException("ID в теле запроса не совпадает с ID в URL");
        }

        User existingUser = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));

        log.debug("Существующий пользователь: {}", existingUser);

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            log.debug("Проверка уникальности email: {}", userDto.getEmail());
            repository.findByEmail(userDto.getEmail()).ifPresent(user -> {
                if (!user.getId().equals(id)) {
                    log.warn("Email {} уже используется пользователем с id {}", userDto.getEmail(), user.getId());
                    throw new InternalServerException("Email " + userDto.getEmail() + " уже используется другим пользователем");
                }
            });
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }

        log.debug("Обновленный пользователь перед сохранением: {}", existingUser);

        try {
            User updatedUser = repository.save(existingUser);
            log.info("Пользователь с id {} успешно обновлен", id);
            return userMapper.toDto(updatedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка целостности данных при обновлении пользователя: {}", e.getMessage());
            throw new InternalServerException("Email уже существует");
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обновлении пользователя: {}", e.getMessage(), e);
            throw new InternalServerException("Ошибка при обновлении пользователя: " + e.getMessage());
        }
    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("Получение пользователя с id: {}", id);
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        log.info("Удаление пользователя с id: {}", id);
        if (!repository.existsById(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        repository.deleteById(id);
        log.info("Пользователь с id {} удален", id);
    }
}