package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotCreatedException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.constans.Settings.BAN_LIST_ADD_LOGIN;

@Slf4j
@Service
public class UserService {
    UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        log.debug("Сервис выполняет запрос в БД на получение списка всех пользователей");
        List<User> listUser = userStorage.getAllUsers();
        if (listUser.isEmpty()) {
            log.debug("Из БД вернулся пустой список пользователей");
        } else {
            log.debug("Из БД успешно получен список пользователей");
        }
        return listUser;
    }

    public User createUser(User user) {
        if (isCheckLoginInBanList(user.getLogin())) {
            throw new ValidationException(Collections.singleton(Map.of("login",
                    String.format("Регистрировать пользователя с таким именем [%s] запрещено", user.getLogin()))));
        }
        log.debug("Сервис выполняет запрос в БД на добавление нового пользователя");
        User createdUser = userStorage.createUser(user);
        if (createdUser != null) {
            log.debug("В БД успешно добавлен новый пользователь {}", createdUser.getLogin());
        } else {
            log.error("Ошибка БД! User is null. По неизвестной причине не получилось добавить нового пользователя");
            throw new NotCreatedException("New user");
        }
        return createdUser;
    }

    public User updateUser(User user) {
        userNotFoundById((user.getId()));
        if (isCheckLoginInBanList(user.getLogin())) {
            throw new ValidationException(Collections.singleton(Map.of("login",
                    String.format("Изменение логина на [%s] запрещено", user.getLogin()))));
        }
        log.debug("Сервис выполняет запрос в БД на обновление данных пользователя с id={}", user.getId());
        User updateUser = userStorage.updateUser(user);
        if (updateUser != null) {
            log.debug("В БД успешно обновлены данные пользователя {}", updateUser.getLogin());
        } else {
            userNotFoundById(0);
        }
        return updateUser;
    }

    public User findUserById(int id) {
        userNotFoundById(id);
        log.debug("Сервис выполняет запрос в БД на получение пользователя ID={}", id);
        User getUser = userStorage.findUserById(id);
        if (getUser != null) {
            log.debug("Из БД успешно получен пользователь с ID={}", id);
        } else {
            userNotFoundById(0);
        }
        return getUser;
    }

    private Boolean isCheckLoginInBanList(String login) {
        return BAN_LIST_ADD_LOGIN.stream().anyMatch(login::equalsIgnoreCase);
    }

    private void userNotFoundById(int id) {
        if (id <= 0) {
            throw new NotFoundException(String.format("User with ID=%d", id));
        }
    }


/*    Создайте UserService, который будет отвечать за такие операции с пользователями, как добавление в друзья,
    удаление из друзей, вывод списка общих друзей. Пока пользователям не надо одобрять заявки в друзья
        — добавляем сразу. То есть если Лена стала другом Саши, то это значит, что Саша теперь друг Лены.*/
}
