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

import static ru.yandex.practicum.filmorate.constans.Settings.*;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        log.info("{} на получение списка всех пользователей", SERVICE_IN_DB);
        List<User> listUser = userStorage.getAllUsers();

        if (listUser.isEmpty()) {
            log.info("{} пустой список пользователей", SERVICE_FROM_DB);
        } else {
            log.info("{} успешно список из [count={}] пользователей", SERVICE_FROM_DB, listUser.size());
        }
        return listUser;
    }

    public User createUser(User user) {
        if (isCheckLoginInBanList(user.getLogin())) {
            throw new ValidationException(Collections.singleton(Map.of("login",
                    String.format("Регистрировать пользователя с таким именем [%s] запрещено", user.getLogin()))));
        }
        log.info("{} на добавление нового пользователя", SERVICE_IN_DB);
        User createdUser = userStorage.createUser(user);

        if (createdUser != null) {
            log.info("{} успешно новый пользователь [{}]", SERVICE_FROM_DB, createdUser.getLogin());
        } else {
            log.error("Ошибка БД! {} [User is null]. " +
                    "По неизвестной причине не получилось нового пользователя", SERVICE_FROM_DB);
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
        log.info("{} на обновление данных пользователя с [ID={}]", SERVICE_IN_DB, idUser);
        User updateUser = userStorage.updateUser(user);

        if (updateUser != null) {
            log.info("{} успешно обновленный пользователь [login={}]", SERVICE_FROM_DB, updateUser.getLogin());
        } else {
            log.error("Ошибка БД! {} [User is null]. " +
                            "По неизвестной причине не получилось обновить данные пользователя [login={}]",
                    SERVICE_FROM_DB, user.getLogin());
            userNotFoundByIdException(idUser);
        }
        return updateUser;
    }

    public User findUserById(int idUser) {
        userNotFoundByIdCheckPositive(idUser);
        log.info("{} на получение пользователя [ID={}]", SERVICE_IN_DB, idUser);
        User getUser = userStorage.findUserById(idUser);

        if (getUser != null) {
            log.info("{} успешно пользователь с [ID={}]", SERVICE_FROM_DB, idUser);
        } else {
            log.error("{} [User is null] пользователя с [ID={}] не существует", SERVICE_FROM_DB, idUser);
            userNotFoundByIdException(idUser);
        }
        return getUser;
    }

    public void addFriend(Integer idUser, Integer idFriend) {
        userNotFoundByIdCheckPositive(idUser);
        userNotFoundByIdCheckPositive(idFriend);
        log.info("{} на добавление в друзья пользователя [ID={}] к пользователю [ID={}]",
                SERVICE_IN_DB, idFriend, idUser);
        User getUser = userStorage.addFriend(idUser, idFriend);
        if (getUser == null) {
            log.error("{} [User is null] пользователя с [ID={}] не существует", SERVICE_FROM_DB, idUser);
            userNotFoundByIdException(idUser);
        }

        User getFriend = userStorage.findUserById(idFriend);
        if (getFriend == null) {
            log.error("{} [User is null] пользователя с [ID={}] не существует", SERVICE_FROM_DB, idFriend);
            userNotFoundByIdException(idFriend);
        }

        Set<Integer> listFriendsUser = getUser.getFriends();
        if (listFriendsUser.isEmpty()) {
            log.error("{} пользователь [ID={}] но друг [ID={}] не добавлен в поле [friends]",
                    SERVICE_FROM_DB, idUser, idFriend);
            throw new NotCreatedException(String.format("New friend for user ID=%d", idUser));
        }

        if (listFriendsUser.contains(idFriend)) {
            log.info("{} успешно обновленный [ID={}] пользователь: добавлен пользователь [ID={}] в друзья",
                    SERVICE_FROM_DB, idUser, idFriend);
            String statusFriendship = getStatusFriendship(idUser, listFriendsUser, idFriend, getFriend.getFriends());
            log.info("Текущий статус дружбы: {}", statusFriendship);
        } else {
            log.error("{} пользователь [ID={}] но друг [ID={}] не найден в поле [friends]",
                    SERVICE_FROM_DB, idUser, idFriend);
            throw new NotCreatedException(String.format("New friend for user ID=%d", idUser));
        }
    }

    public void removeFriend(int idUser, int idFriend) {
        userNotFoundByIdCheckPositive(idUser);
        userNotFoundByIdCheckPositive(idFriend);
        log.info("{} на удаление из друзей пользователя [ID={}] у пользователя [ID={}]",
                SERVICE_IN_DB, idFriend, idUser);

        User getUser = userStorage.removeFriend(idUser, idFriend);
        if (getUser == null) {
            log.error("{} [User is null] пользователя с [ID={}] не существует", SERVICE_FROM_DB, idUser);
            userNotFoundByIdException(idUser);
        }

        User getFriend = userStorage.findUserById(idFriend);
        if (getFriend == null) {
            log.error("{} [User is null] пользователя с [ID={}] не существует", SERVICE_FROM_DB, idFriend);
            userNotFoundByIdException(idFriend);
        }
        Set<Integer> listFriendsUser = getUser.getFriends();

        if (listFriendsUser.contains(idFriend)) {
            log.error("{} пользователь [ID={}] но друг [ID={}] не был удален в поле [friends]",
                    SERVICE_FROM_DB, idUser, idFriend);
            throw new NotRemovedException(String.format("Friend ID=%d for user ID=%d", idFriend, idUser));
        } else {
            log.info("{} успешно обновленный [ID={}] пользователь: " +
                    "удалён пользователь [ID={}] из друзей", SERVICE_FROM_DB, idUser, idFriend);
            String statusFriendship = getStatusFriendship(idUser, listFriendsUser, idFriend, getFriend.getFriends());
            log.info("Текущий статус дружбы: {}", statusFriendship);
        }
    }

    public List<User> getAllFriendsByUserId(int idUser) {
        userNotFoundByIdCheckPositive(idUser);
        log.info("{} на получение списка друзей пользователя [ID={}]", SERVICE_IN_DB, idUser);
        List<User> listAllFriendsByUserId = userStorage.getAllFriendsByUserId(idUser);

        if (listAllFriendsByUserId.isEmpty()) {
            log.info("{} пустой список друзей пользователя [ID={}]", SERVICE_FROM_DB, idUser);
        } else {
            log.info("{} успешно список из [count={}] друзей пользователя [ID={}]",
                    SERVICE_FROM_DB, listAllFriendsByUserId.size(), idUser);
        }
        return listAllFriendsByUserId;
    }

    public List<User> getAllCommonFriendsByUserId(int idUser, int idFriend) {
        userNotFoundByIdCheckPositive(idUser);
        userNotFoundByIdCheckPositive(idFriend);
        log.info("{} на получение общего списка друзей пользователя [ID={}] " +
                "с пользователем [ID={}]", SERVICE_IN_DB, idUser, idFriend);
        List<User> listAllCommonFriendsByUserId = userStorage.getAllCommonFriendsByUserId(idUser, idFriend);

        if (listAllCommonFriendsByUserId.isEmpty()) {
            log.info("{} пустой список общих друзей пользователя [ID={}] с пользователем [ID={}]",
                    SERVICE_FROM_DB, idUser, idFriend);
        } else {
            log.info("{} успешно список из [count={}] общих друзей пользователя [ID={}] и [ID={}]",
                    SERVICE_FROM_DB, listAllCommonFriendsByUserId.size(), idUser, idFriend);
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
