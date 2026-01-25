package ru.practicum.shareit.validation;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

@Slf4j
public class ValidationTool {

    private ValidationTool() {
        throw new UnsupportedOperationException();
    }

    public static void checkId(Long id, String level, String description) {
        if (id == null || id < 1L) {
            throw new ValidationException("[" + level + "]: " + description);
        }
    }

    public static void userCheck(User user, String level) {
        log.info("Запущен процесс валидации объекта User");
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Процесс валидации объекта User не пройден - имя некорректно");
            throw new ValidationException("[" + level + "]: имя не может быть пустым или равняться null");
        }

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Процесс валидации объекта User не пройден - Email некорректен");
            throw new ValidationException("[" + level + "]: Электронная почта не может быть пустой и должна содержать символ @");
        }
        log.info("Процесс валидации объекта User пройден успешно");
    }
}
