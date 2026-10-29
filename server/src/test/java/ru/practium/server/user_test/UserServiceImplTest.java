package ru.practium.server.user_test;

import io.qameta.allure.Epic;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.dto.exception.InternalServerException;
import ru.practicum.dto.exception.NotFoundException;
import ru.practicum.dto.user.UserDto;
import ru.practicum.server.user.UserServiceImpl;
import ru.practicum.server.user.mapper.UserMapper;
import ru.practicum.server.user.model.User;
import ru.practicum.server.user.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Иван");
        user.setEmail("ivan@example.com");

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Иван");
        userDto.setEmail("ivan@example.com");
    }

    // ----------------------------- CREATE -----------------------------

    @Test
    @DisplayName("create: успешное создание пользователя")
    @Epic("===Epic_create===")
    @Severity(value = SeverityLevel.BLOCKER)
    void create_shouldReturnSavedUserDto_whenEmailIsUnique() {
        UserDto request = new UserDto();
        request.setName("Иван");
        request.setEmail("ivan@example.com");

        User toSave = new User();
        toSave.setName("Иван");
        toSave.setEmail("ivan@example.com");

        User saved = new User();
        saved.setId(1L);
        saved.setName("Иван");
        saved.setEmail("ivan@example.com");

        when(repository.findByEmail("ivan@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(toSave);
        when(repository.save(toSave)).thenReturn(saved);
        when(userMapper.toDto(saved)).thenReturn(userDto);

        UserDto result = userService.create(request);

        assertThat(result).isEqualTo(userDto);
        verify(repository).findByEmail("ivan@example.com");
        verify(userMapper).toEntity(request);
        verify(repository).save(toSave);
    }

    @Test
    @Epic("===Epic_create===")
    @DisplayName("create: ошибка, если email уже существует")
    void create_shouldThrow_whenEmailAlreadyExists() {
        UserDto request = new UserDto();
        request.setName("Иван");
        request.setEmail("ivan@example.com");

        when(repository.findByEmail("ivan@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("уже существует");

        verify(repository, never()).save(any());
    }

    @Test
    @Epic("===Epic_create===")
    @DisplayName("create: DataIntegrityViolationException при сохранении")
    void create_shouldThrowInternalServerException_whenSaveFails() {
        UserDto request = new UserDto();
        request.setName("Иван");
        request.setEmail("ivan@example.com");

        User toSave = new User();
        toSave.setName("Иван");
        toSave.setEmail("ivan@example.com");

        when(repository.findByEmail("ivan@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(toSave);
        when(repository.save(toSave)).thenThrow(new DataIntegrityViolationException("dup"));

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("уже существует");
    }


    @Test
    @Epic("===Epic_update===")
    @DisplayName("update: успешное обновление пользователя")
    void update_shouldReturnUpdatedUserDto() {
        UserDto request = new UserDto();
        request.setName("Петр");
        request.setEmail("petr@example.com");

        User updated = new User();
        updated.setId(1L);
        updated.setName("Петр");
        updated.setEmail("petr@example.com");

        UserDto expected = new UserDto();
        expected.setId(1L);
        expected.setName("Петр");
        expected.setEmail("petr@example.com");

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.findByEmail("petr@example.com")).thenReturn(Optional.empty());
        when(repository.save(user)).thenReturn(updated);
        when(userMapper.toDto(updated)).thenReturn(expected);

        UserDto result = userService.update(1L, request);

        assertThat(result).isEqualTo(expected);
        assertThat(user.getName()).isEqualTo("Петр");
        assertThat(user.getEmail()).isEqualTo("petr@example.com");
        verify(repository).save(user);
    }

    @Test
    @Epic("===Epic_update===")
    @DisplayName("update: ошибка, если ID в теле не совпадает с ID в пути")
    void update_shouldThrow_whenIdMismatch() {
        UserDto request = new UserDto();
        request.setId(2L);
        request.setName("Петр");
        request.setEmail("petr@example.com");

        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("не совпадает");

        verify(repository, never()).findById(anyLong());
        verify(repository, never()).save(any());
    }

    @Test
    @Epic("===Epic_update===")
    @DisplayName("update: ошибка, если пользователь не найден")
    void update_shouldThrowNotFound_whenUserNotExists() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(1L, userDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");

        verify(repository, never()).save(any());
    }

    @Test
    @Epic("===Epic_update===")
    @DisplayName("update: ошибка, если новый email уже используется другим пользователем")
    void update_shouldThrow_whenNewEmailTakenByAnotherUser() {
        UserDto request = new UserDto();
        request.setName("Иван");
        request.setEmail("new@example.com");

        User other = new User();
        other.setId(2L);
        other.setName("Другой");
        other.setEmail("new@example.com");

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.findByEmail("new@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("уже используется");

        verify(repository, never()).save(any());
    }

    @Test
    @Epic("===Epic_update===")
    @DisplayName("update: не меняет email, если он не передан")
    void update_shouldKeepEmail_whenEmailIsNull() {
        UserDto request = new UserDto();
        request.setName("Петр");

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        userService.update(1L, request);

        assertThat(user.getEmail()).isEqualTo("ivan@example.com");
        assertThat(user.getName()).isEqualTo("Петр");
        verify(repository, never()).findByEmail(any());
    }

    @Test
    @Epic("===Epic_update===")
    @DisplayName("update: ошибка целостности при сохранении превращается в InternalServerException")
    void update_shouldWrapDataIntegrityViolation() {
        UserDto request = new UserDto();
        request.setName("Петр");
        request.setEmail("ivan@example.com"); // тот же email — проверка не срабатывает

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenThrow(new DataIntegrityViolationException("dup"));

        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Email уже существует");
    }

    @Test
    @DisplayName("getUserById: возвращает пользователя, если найден")
    void getUserById_shouldReturnUserDto() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserById(1L);

        assertThat(result).isEqualTo(userDto);
        verify(repository).findById(1L);
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("getUserById: ошибка NotFound, если пользователь не найден")
    void getUserById_shouldThrowNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");

        verify(userMapper, never()).toDto(any());
    }

    // ----------------------------- DELETE -----------------------------

    @Test
    @DisplayName("deleteUserById: успешное удаление")
    void deleteUserById_shouldDelete_whenUserExists() {
        when(repository.existsById(1L)).thenReturn(true);

        userService.deleteUserById(1L);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUserById: ошибка NotFound, если пользователь не найден")
    void deleteUserById_shouldThrowNotFound_whenUserNotExists() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUserById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");

        verify(repository, never()).deleteById(anyLong());
    }
}
