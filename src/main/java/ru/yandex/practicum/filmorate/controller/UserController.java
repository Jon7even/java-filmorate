package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int idGenerator = 1;

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Сделан запрос на получение списка всех пользователей");
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        if (isCheckName(user)) {
            user.setName(user.getLogin());
        }
        if (isCheckEmailInDateBase(user.getEmail())){
            throw new ValidationException("Пользователь с таким email - "+ user.getEmail() +" уже существует");
        }
        user.setId(idGenerator++);
        users.put(user.getId(), user);
        log.info("В БД успешно добавлен пользователь с ID={}", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        int userId = user.getId();
        if (users.containsKey(userId)) {
            User oldUser = users.get(userId);
            if (isCheckName(user)) {
                user.setName(user.getLogin());
            }
            users.put(userId, user);
            if (user.equals(oldUser)) {
                log.warn("При обновлении данных аккаунта, пользователь с ID={} не дал новых данных " +
                        "если это сообщение повторится, на это стоит обратить внимание", userId);
            }
            if (isCheckEmailInDateBase(user.getEmail())){
                throw new ValidationException("Данный email - " + user.getEmail() + " уже находится в БД");
            }
            log.info("Пользователь с ID={} успешно обновлен!\n Старый аккаунт: {},\n Новый аккаунт: {}",
                    userId, oldUser.toString(), users.get(userId));
            return user;
        } else {
            throw new ValidationException("Пользователя с таким ID=" + userId + " не существует");
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
        return users.values().stream().anyMatch(user -> user.getEmail().equals(emailCheck));
    }
}
