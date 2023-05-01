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
            log.error("Ошибка БД! User is null.");
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
            log.error("Ошибка БД! User is null.");
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
            log.error("Ошибка БД! User is null.");
            userNotFoundById(0);
        }
        return getUser;
    }

    public void addFriend(int idUser, int idFriend) {
        userNotFoundById(idUser);
        userNotFoundById(idFriend);
        log.debug("Сервис выполняет запрос в БД на добавление в друзья пользователя ID={} к пользователю ID={}",
                idFriend, idUser);
        User getUser = userStorage.addFriend(idUser, idFriend);
        if (getUser.getFriends().contains(idFriend)) {
            log.debug("В БД успешно добавлен друг ID={} пользователю ID={}", idFriend, idUser);
        } else {
            log.error("Ошибка БД! User is null.");
            userNotFoundById(0);
        }
    }

    public void removeFriend(int idUser, int idFriend) {
        userNotFoundById(idUser);
        userNotFoundById(idFriend);
        log.debug("Сервис выполняет запрос в БД на удаление из друзей пользователя ID={} у пользователя ID={}",
                idFriend, idUser);
        userStorage.removeFriend(idUser, idFriend);
    }

    public List<User> getAllFriendsByUserId(int idUser) {
        userNotFoundById(idUser);
        log.debug("Сервис выполняет запрос в БД на получение списка друзей пользователя ID={}", idUser);
        List<User> listAllFriendsByUserId = userStorage.getAllFriendsByUserId(idUser);
        if (listAllFriendsByUserId.isEmpty()) {
            log.debug("Из БД вернулся пустой список друзей пользователя ID={}", idUser);
        } else {
            log.debug("Из БД успешно получен список друзей пользователя ID={}", idUser);
        }
        return listAllFriendsByUserId;
    }

    public List<User> getAllCommonFriendsByUserId(int idUser, int idFriend) {
        userNotFoundById(idUser);
        userNotFoundById(idFriend);
        log.debug("Сервис выполняет запрос в БД на получение общего списка друзей пользователя ID={} " +
                "с пользователем ID={}", idUser, idFriend);
        List<User> listAllCommonFriendsByUserId = userStorage.getAllCommonFriendsByUserId(idUser, idFriend);

        if (listAllCommonFriendsByUserId.isEmpty()) {
            log.debug("Из БД вернулся пустой список общих друзей пользователя ID={} с пользователем ID={}",
                    idUser, idFriend);
        } else {
            log.debug("Из БД успешно получен список общих друзей пользователя ID={} и ID={}", idUser, idFriend);
        }
        return listAllCommonFriendsByUserId;
    }

    private Boolean isCheckLoginInBanList(String login) {
        return BAN_LIST_ADD_LOGIN.stream().anyMatch(login::equalsIgnoreCase);
    }

    private void userNotFoundById(int id) {
        if (id <= 0) {
            throw new NotFoundException(String.format("User with ID=%d", id));
        }
    }
}
