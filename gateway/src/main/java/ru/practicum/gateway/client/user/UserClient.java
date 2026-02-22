package ru.practicum.gateway.client.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.user.UserDto;
import ru.practicum.gateway.base.BaseClient;

@Slf4j
@Service
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory()) // Возвращаем HttpComponents
                        .build()
        );
    }

    public ResponseEntity<Object> getUserList() {
        log.info("Отправка запроса на получение списка всех пользователей");
        return get("");
    }

    public ResponseEntity<Object> getUserDto(Long id) {
        log.info("Отправка запроса на получение пользователя с id={}", id);
        return get("/" + id);
    }

    public ResponseEntity<Object> create(UserDto userDto) {
        log.info("Отправка запроса на создание пользователя: {}", userDto);
        return post("", userDto);
    }

    public ResponseEntity<Object> update(Long userId, UserDto userDto) {
        log.info("Отправка запроса на обновление пользователя с ID={}, данные: {}", userId, userDto);
        return patch("/" + userId, userDto);
    }

    public ResponseEntity<Object> delete(Long id) {
        log.info("Отправка запроса на удаление пользователя с id={}", id);
        return delete("/" + id);
    }
}