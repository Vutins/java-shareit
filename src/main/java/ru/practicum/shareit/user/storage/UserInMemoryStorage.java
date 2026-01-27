package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class UserInMemoryStorage implements UserStorage {

    private static Long userId = 0L;
    private HashMap<Long, User> usersMap = new HashMap<>();

    @Override
    public User create(User user) {
        user.setId(nextId());
        for (Map.Entry<Long, User> entry : usersMap.entrySet()) {
            if (entry.getValue().getEmail().equals(user.getEmail())) {
                throw new InternalServerException("user с таким email уже существует");
            }
        }
        usersMap.put(user.getId(), user);

        return user.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @Override
    public User update(Long id, User user) {
        usersMap.put(id, user);
        return user;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(usersMap.get(id))
                .map(user -> User.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .build());
    }

    @Override
    public boolean deleteUserById(Long id) {
        usersMap.remove(id);
        return !usersMap.containsKey(id);
    }

    private static Long nextId() {
        return ++userId;
    }
}
