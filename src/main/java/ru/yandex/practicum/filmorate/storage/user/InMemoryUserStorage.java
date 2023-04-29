package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.IdGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.constans.Settings.BAN_LIST_FIND_LOGIN;

@Slf4j
@Service
public class InMemoryUserStorage implements UserStorage {
    IdGenerator id;
    private final Map<Integer, User> users;

    public InMemoryUserStorage() {
        this.id = new IdGenerator();
        this.users = new HashMap<>();
    }

    public List<User> getAllUsers() {
        log.info("В БД выполняется запрос на получение списка всех пользователей. " +
                "*Работает фильтр BanListFindLogin.properties");
        return users.values().stream().filter(user -> !BAN_LIST_FIND_LOGIN.contains(user.getLogin()))
                .collect(Collectors.toList());
    }

    public User createUser(User user) {
        if (isCheckLoginOnDuplicate(user.getLogin())) {
            throw new ValidationException(Collections.singleton(Map.of("login",
                    String.format("Пользователь с таким логином [%s] уже имеется в системе", user.getLogin()))));
        }
        if (isCheckEmailInDateBase(user.getEmail())) {
            throw new ValidationException(Collections.singleton(Map.of("login",
                    String.format("Пользователь с таким email [%s] уже имеется в системе", user.getEmail()))));
        }
        if (isCheckName(user)) {
            user.setName(user.getLogin());
        }
        user.setId(id.getIdGenerator());
        users.put(user.getId(), user);
        log.info("В БД добавлен новый пользователь {}", users.get(user.getId()));
        return user;
    }

    public User updateUser(User user) {
        int userId = user.getId();
        if (users.containsKey(userId)) {
            if (isCheckEmailInDateBase(user.getEmail())) {
                throw new ValidationException(Collections.singleton(Map.of("email",
                        String.format("Данный email [%s] уже находится в БД", user.getEmail()))));
            }
            User oldUser = users.get(userId);
            if (isCheckName(user)) {
                user.setName(user.getLogin());
            }
            users.put(userId, user);
            if (user.equals(oldUser)) {
                log.warn("При обновлении данных аккаунта, пользователь с ID={} не дал новых данных " +
                        "если это сообщение повторится, на это стоит обратить внимание", userId);
            }
            log.info("Пользователь с ID={} успешно обновлен!\n Старый аккаунт: {},\n Новый аккаунт: {}",
                    userId, oldUser.toString(), users.get(userId));
            return user;
        } else {
            throw new ValidationException(Collections.singleton(Map.of("id",
                    String.format("Пользователя с таким [ID=%d] не существует", user.getId()))));
        }
    }

    private Boolean isCheckName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Пользователь не указал имени. Поле \"name\" берется из логина - {}", user.getLogin());
            return true;
        }
        return false;
    }

    private Boolean isCheckEmailInDateBase(String emailCheck) {
        return users.values().stream().anyMatch(user -> user.getEmail().equalsIgnoreCase(emailCheck));
    }

    private Boolean isCheckLoginOnDuplicate(String login) {
        return users.values().stream().anyMatch(user -> user.getLogin().equalsIgnoreCase(login));
    }

    public void clearRepository() {
        id.resetIdGenerator();
        users.clear();
    }
}
