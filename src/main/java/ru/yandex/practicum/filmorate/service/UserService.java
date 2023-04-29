package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        User requestUser = userStorage.createUser(user);
        if (requestUser != null) {
            log.debug("В БД успешно добавлен новый пользователь {}", requestUser.getLogin());
        } else {
            log.error("БД вернула null. По неизвестной причине не получилось добавить нового пользователя");
        }
        return requestUser;
    }

    public User updateUser(User user) {
        if (user.getId() <= 0) {
            throw new ValidationException(Collections.singleton(Map.of("id",
                    "Поле должно быть положительным")));
        }
        if (isCheckLoginInBanList(user.getLogin())) {
            throw new ValidationException(Collections.singleton(Map.of("login",
                    String.format("Изменение логина на [%s] запрещено", user.getLogin()))));
        }
        log.debug("Сервис выполняет запрос в БД на обновление данных пользователя с id={}", user.getId());
        User updateUser = userStorage.updateUser(user);
        if (updateUser != null) {
            log.debug("В БД успешно обновлены данные пользователя {}", updateUser.getLogin());
        } else {
            log.error("БД вернула null. По неизвестной причине не получилось обновить пользователя");
        }
        return updateUser;
    }

    private Boolean isCheckLoginInBanList(String login) {
        return BAN_LIST_ADD_LOGIN.stream().anyMatch(login::equalsIgnoreCase);
    }

/*    Создайте UserService, который будет отвечать за такие операции с пользователями, как добавление в друзья,
    удаление из друзей, вывод списка общих друзей. Пока пользователям не надо одобрять заявки в друзья
        — добавляем сразу. То есть если Лена стала другом Саши, то это значит, что Саша теперь друг Лены.*/
}
