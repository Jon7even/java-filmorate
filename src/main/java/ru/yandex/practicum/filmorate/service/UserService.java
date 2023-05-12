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
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        log.info("Сервис выполняет запрос в БД на получение списка всех пользователей");
        List<User> listUser = userStorage.getAllUsers();

        if (listUser.isEmpty()) {
            log.info("В сервис из БД вернулся пустой список пользователей");
        } else {
            log.info("В сервис из БД успешно получен список из [count={}] пользователей", listUser.size());
        }
        return listUser;
    }

    public User createUser(User user) {
        if (isCheckLoginInBanList(user.getLogin())) {
            throw new ValidationException(Collections.singleton(Map.of("login",
                    String.format("Регистрировать пользователя с таким именем [%s] запрещено", user.getLogin()))));
        }
        log.info("Сервис выполняет запрос в БД на добавление нового пользователя");
        User createdUser = userStorage.createUser(user);

        if (createdUser != null) {
            log.info("В сервис из БД успешно вернулся новый пользователь [{}]", createdUser.getLogin());
        } else {
            log.error("Ошибка БД! В сервис из БД вернулся [User is null]. " +
                    "По неизвестной причине не получилось нового пользователя");
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
        log.info("Сервис выполняет запрос в БД на обновление данных пользователя с [ID={}]", user.getId());
        User updateUser = userStorage.updateUser(user);

        if (updateUser != null) {
            log.info("В сервис из БД успешно вернулся обновленный пользователь [login={}]", updateUser.getLogin());
        } else {
            log.error("Ошибка БД! В сервис из БД вернулся [User is null]. " +
                    "По неизвестной причине не получилось обновить данные пользователя [login={}]", user.getLogin());
            userNotFoundById(0);
        }
        return updateUser;
    }

    public User findUserById(int idUser) {
        userNotFoundById(idUser);
        log.info("Сервис выполняет запрос в БД на получение пользователя [ID={}]", idUser);
        User getUser = userStorage.findUserById(idUser);

        if (getUser != null) {
            log.info("Из БД успешно получен пользователь с [ID={}]", idUser);
        } else {
            log.error("В сервис из БД вернулся [User is null] пользователя с [ID={}] не существует", idUser);
            userNotFoundById(0);
        }
        return getUser;
    }

    public void addFriend(int idUser, int idFriend) {
        userNotFoundById(idUser);
        userNotFoundById(idFriend);
        log.info("Сервис выполняет запрос в БД на добавление в друзья пользователя [ID={}] к пользователю [ID={}]",
                idFriend, idUser);
        User getUser = userStorage.addFriend(idUser, idFriend);

        if (getUser.getFriends().contains(idFriend)) {
            log.info("В БД успешно обновлены данные [ID={}] пользователя: добавлен пользователь [ID={}] в друзья",
                    idUser, idFriend);
        } else {
            log.error("В сервис из БД вернулся [User is null] пользователя с [ID={}] не существует", idUser);
            userNotFoundById(0);
        }
    }

    public void removeFriend(int idUser, int idFriend) {
        userNotFoundById(idUser);
        userNotFoundById(idFriend);
        log.info("Сервис выполняет запрос в БД на удаление из друзей пользователя [ID={}] у пользователя [ID={}]",
                idFriend, idUser);
        User getUser = userStorage.removeFriend(idUser, idFriend);

        if (getUser.getFriends().contains(idFriend)) {
            log.error("В сервис из БД вернулся [User is null] пользователя с [ID={}] не существует", idUser);
            userNotFoundById(0);
        } else {
            log.info("В сервис из БД успешно пришли обновленные данные [ID={}] пользователя: " +
                    "удалён пользователь [ID={}] из друзей", idUser, idFriend);
        }
    }

    public List<User> getAllFriendsByUserId(int idUser) {
        userNotFoundById(idUser);
        log.info("Сервис выполняет запрос в БД на получение списка друзей пользователя [ID={}]", idUser);
        List<User> listAllFriendsByUserId = userStorage.getAllFriendsByUserId(idUser);

        if (listAllFriendsByUserId.isEmpty()) {
            log.info("В сервис из БД вернулся пустой список друзей пользователя [ID={}]", idUser);
        } else {
            log.info("В сервис из БД успешно вернулся список из [count={}] друзей пользователя [ID={}]",
                    listAllFriendsByUserId.size(), idUser);
        }
        return listAllFriendsByUserId;
    }

    public List<User> getAllCommonFriendsByUserId(int idUser, int idFriend) {
        userNotFoundById(idUser);
        userNotFoundById(idFriend);
        log.info("Сервис выполняет запрос в БД на получение общего списка друзей пользователя [ID={}] " +
                "с пользователем [ID={}]", idUser, idFriend);
        List<User> listAllCommonFriendsByUserId = userStorage.getAllCommonFriendsByUserId(idUser, idFriend);

        if (listAllCommonFriendsByUserId.isEmpty()) {
            log.info("В сервис из БД вернулся пустой список общих друзей пользователя [ID={}] с пользователем [ID={}]",
                    idUser, idFriend);
        } else {
            log.info("В сервис из БД успешно получен список из [count={}] общих друзей пользователя [ID={}] и [ID={}]",
                    listAllCommonFriendsByUserId.size(), idUser, idFriend);
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
