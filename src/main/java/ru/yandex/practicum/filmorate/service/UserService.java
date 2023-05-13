package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        int idUser = user.getId();
        userNotFoundByIdCheckPositive((idUser));

        if (isCheckLoginInBanList(user.getLogin())) {
            throw new ValidationException(Collections.singleton(Map.of("login",
                    String.format("Изменение логина на [%s] запрещено", user.getLogin()))));
        }
        log.info("Сервис выполняет запрос в БД на обновление данных пользователя с [ID={}]", idUser);
        User updateUser = userStorage.updateUser(user);

        if (updateUser != null) {
            log.info("В сервис из БД успешно вернулся обновленный пользователь [login={}]", updateUser.getLogin());
        } else {
            log.error("Ошибка БД! В сервис из БД вернулся [User is null]. " +
                    "По неизвестной причине не получилось обновить данные пользователя [login={}]", user.getLogin());
            userNotFoundByIdException(idUser);
        }
        return updateUser;
    }

    public User findUserById(int idUser) {
        userNotFoundByIdCheckPositive(idUser);
        log.info("Сервис выполняет запрос в БД на получение пользователя [ID={}]", idUser);
        User getUser = userStorage.findUserById(idUser);

        if (getUser != null) {
            log.info("Из БД успешно получен пользователь с [ID={}]", idUser);
        } else {
            log.error("В сервис из БД вернулся [User is null] пользователя с [ID={}] не существует", idUser);
            userNotFoundByIdException(idUser);
        }
        return getUser;
    }

    public void addFriend(Integer idUser, Integer idFriend) {
        userNotFoundByIdCheckPositive(idUser);
        userNotFoundByIdCheckPositive(idFriend);
        log.info("Сервис выполняет запрос в БД на добавление в друзья пользователя [ID={}] к пользователю [ID={}]",
                idFriend, idUser);
        User getUser = userStorage.addFriend(idUser, idFriend);
        if (getUser == null) {
            log.error("В сервис из БД вернулся [User is null] пользователя с [ID={}] не существует", idUser);
            userNotFoundByIdException(idUser);
        }

        User getFriend = userStorage.findUserById(idFriend);
        if (getFriend == null) {
            log.error("В сервис из БД вернулся [User is null] пользователя с [ID={}] не существует", idFriend);
            userNotFoundByIdException(idFriend);
        }

        Set<Integer> listFriendsUser = getUser.getFriends();
        if (listFriendsUser.isEmpty()) {
            log.error("В сервис из БД вернулся пользователь [ID={}] но друг [ID={}] не добавлен в поле [friends]",
                    idUser, idFriend);
            throw new NotCreatedException(String.format("New friend for user ID=%d", idUser));
        }

        if (listFriendsUser.contains(idFriend)) {
            log.info("В БД успешно обновлены данные [ID={}] пользователя: добавлен пользователь [ID={}] в друзья",
                    idUser, idFriend);
            String statusFriendship = getStatusFriendship(idUser, listFriendsUser, idFriend, getFriend.getFriends());
            log.info("Текущий статус дружбы: {}", statusFriendship);
        } else {
            log.error("В сервис из БД вернулся пользователь [ID={}] но друг [ID={}] не найден в поле [friends]",
                    idUser, idFriend);
            throw new NotCreatedException(String.format("New friend for user ID=%d", idUser));
        }
    }

    public void removeFriend(int idUser, int idFriend) {
        userNotFoundByIdCheckPositive(idUser);
        userNotFoundByIdCheckPositive(idFriend);
        log.info("Сервис выполняет запрос в БД на удаление из друзей пользователя [ID={}] у пользователя [ID={}]",
                idFriend, idUser);

        User getUser = userStorage.removeFriend(idUser, idFriend);
        if (getUser == null) {
            log.error("В сервис из БД вернулся [User is null] пользователя с [ID={}] не существует", idUser);
            userNotFoundByIdException(idUser);
        }

        User getFriend = userStorage.findUserById(idFriend);
        if (getFriend == null) {
            log.error("В сервис из БД вернулся [User is null] пользователя с [ID={}] не существует", idFriend);
            userNotFoundByIdException(idFriend);
        }
        Set<Integer> listFriendsUser = getUser.getFriends();

        if (listFriendsUser.contains(idFriend)) {
            log.error("В сервис из БД вернулся пользователь [ID={}] но друг [ID={}] не был удален в поле [friends]",
                    idUser, idFriend);
            throw new NotRemovedException(String.format("Friend ID=%d for user ID=%d", idFriend, idUser));
        } else {
            log.info("В сервис из БД успешно пришли обновленные данные [ID={}] пользователя: " +
                    "удалён пользователь [ID={}] из друзей", idUser, idFriend);
            String statusFriendship = getStatusFriendship(idUser, listFriendsUser, idFriend, getFriend.getFriends());
            log.info("Текущий статус дружбы: {}", statusFriendship);
        }
    }

    public List<User> getAllFriendsByUserId(int idUser) {
        userNotFoundByIdCheckPositive(idUser);
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
        userNotFoundByIdCheckPositive(idUser);
        userNotFoundByIdCheckPositive(idFriend);
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

    private String getStatusFriendship(int idUser, Set<Integer> listFriendsUser,
                                       int idFriend, Set<Integer> listFriendsFriend) {
        if (listFriendsUser.contains(idFriend) && listFriendsFriend.contains(idUser)) {
            return String.format("Дружба между пользователями [ID=%d] и [ID=%d] подтверждена", idUser, idFriend);
        } else if (listFriendsUser.contains(idFriend) && !listFriendsFriend.contains(idUser)) {
            return String.format("Пользователь [ID=%d] ожидает подтверждения дружбы от пользователя [ID=%d]",
                    idUser, idFriend);
        } else if (!listFriendsUser.contains(idFriend) && listFriendsFriend.contains(idUser)) {
            return String.format("Пользователь [ID=%d] ожидает подтверждения дружбы от пользователя [ID=%d]",
                    idFriend, idUser);
        } else if (!listFriendsUser.contains(idFriend) && !listFriendsFriend.contains(idUser)) {
            return String.format("Пользователи [ID=%d] и [ID=%d] больше не друзья",
                    idFriend, idUser);
        } else {
            throw new UnknownException("Status Friendship");
        }
    }

    private Boolean isCheckLoginInBanList(String login) {
        return BAN_LIST_ADD_LOGIN.stream().anyMatch(login::equalsIgnoreCase);
    }

    private void userNotFoundByIdCheckPositive(int idUser) {
        if (idUser <= 0) {
            throw new NotFoundException(String.format("User with ID=%d", idUser));
        }
    }

    private void userNotFoundByIdException(int idUser) {
        throw new NotFoundException(String.format("User with ID=%d", idUser));
    }
}
