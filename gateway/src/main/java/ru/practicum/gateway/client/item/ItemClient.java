package ru.practicum.gateway.client.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;

import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.item.ItemDto;
import ru.practicum.gateway.base.BaseClient;

import java.util.Map;

@Slf4j
@Service
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getItemById(Long itemId) {
        log.info("Отправка запроса на получение информации о вещи с ID={}", itemId);
        return get("/" + itemId);
    }

    public ResponseEntity<Object> getItemByUserId(Long userId) {
        log.info("Отправка запроса на получение списка вещей пользователя с ID={}", userId);
        return get("", userId);
    }

    public ResponseEntity<Object> searchText(String text) {
        log.info("Отправка запроса на поиск вещей по тексту: {}", text);
        if (text == null || text.isBlank()) {
            return get("/search");
        }
        return get("/search?text=" + text);
    }

    public ResponseEntity<Object> create(Long userId, ItemDto itemDto) {
        log.info("Отправка запроса на создание новой вещи для пользователя с ID={}, данные: {}", userId, itemDto);
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> update(Long itemId, Long userId, Map<String, Object> updates) {
        log.info("Отправка запроса на обновление вещи с ID={} для пользователя с ID={}, данные: {}", itemId, userId, updates);
        return patch("/" + itemId, userId, updates);
    }

    public ResponseEntity<Object> addComment(Long itemId, Long userId, CommentDto commentDto) {
        log.info("Отправка запроса на добавление комментария к вещи с ID={} от пользователя с ID={}, текст: {}",
                itemId, userId, commentDto.getText());
        return post("/" + itemId + "/comment", userId, commentDto);

    }

    public ResponseEntity<Object> deleteItem(Long itemId) {
        return delete("/" + itemId);
    }

    public ResponseEntity<Object> getItemDtoWithBookingsAndComments(Long itemId, Long userId) {
        log.info("Запрос информации о вещи с ID={} для пользователя {}", itemId, userId);
        log.debug("Полный URL: {}/{}", rest.getUriTemplateHandler().toString(), itemId);

        ResponseEntity<Object> response = getWithHeaders("/" + itemId, userId);

        log.debug("Ответ от сервера: статус {}, тело: {}",
                response.getStatusCode(), response.getBody());

        return response;
    }
}