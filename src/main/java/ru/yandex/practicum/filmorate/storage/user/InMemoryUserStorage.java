package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotCreatedException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.NotRemovedException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.IdGenerator;

import java.util.*;
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
        log.info("В БД выполняется запрос на добавление нового пользователя");

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
        int newId = id.getIdGenerator();

        if (newId <= 0) {
            log.error("Неизвестная ошибка генерации ID.");
            throw new NotCreatedException("New user");
        }

        user.setId(newId);
        users.put(user.getId(), user);
        User createdUser = users.get(user.getId());
        log.info("В БД добавлен новый пользователь {}", createdUser);
        return createdUser;
    }

    public User updateUser(User user) {
        int userId = user.getId();
        log.info("В БД выполняется запрос на обновление данных пользователя с ID={}", userId);

        if (users.containsKey(userId)) {
            if (isCheckEmailInDateBase(user.getEmail())) {
                log.warn("Пользователь сменил email {} но он уже есть в БД", user.getEmail());
            }
            User oldUser = users.get(userId);

            if (isCheckName(user)) {
                user.setName(user.getLogin());
            }
            users.put(userId, user);
            User updateUser = users.get(userId);

            if (updateUser.equals(oldUser)) {
                log.warn("При обновлении данных аккаунта, пользователь с ID={} не дал новых данных " +
                        "если это сообщение повторится, на это стоит обратить внимание", userId);
            }
            log.info("Пользователь с ID={} успешно обновлен!\n Старый аккаунт: {},\n Новый аккаунт: {}",
                    userId, oldUser, updateUser);
            return updateUser;
        } else {
            throw new NotFoundException(String.format("User with ID=%d", userId));
        }
    }

    public User findUserById(int id) {
        log.info("В БД выполняется запрос на получение данных пользователя с ID={}", id);
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            throw new NotFoundException(String.format("User with ID=%d", id));
        }
    }

    public User addFriend(int idUser, int idFriend) {
        log.info("В БД выполняется запрос на добавление друга ID={} пользователю с ID={}", idFriend, idUser);
        User findUser = findUserById(idUser);
        User friendUser = findUserById(idFriend);
        findUser.addFriend(idFriend);
        friendUser.addFriend(idUser);
        updateUser(findUser);
        updateUser(friendUser);
        User addedFriend = findUserById(idUser);
        if (addedFriend.getFriends().contains(idFriend)) {
            log.info("Пользователю ID={} успешно добавлен друг ID={}", idUser, idFriend);
            return addedFriend;
        } else {
            log.error("Ошибка БД. Пользователю ID={} не добавлен друг ID={}", idUser, idFriend);
            throw new NotCreatedException(String.format("New friend for user ID=%d", idUser));
        }
    }

    public void removeFriend(int idUser, int idFriend) {
        log.info("В БД выполняется запрос на удаление друга ID={} у пользователя с ID={}", idFriend, idUser);
        User findUser = findUserById(idUser);
        User friendUser = findUserById(idFriend);

        if (findUser.getFriends().contains(idFriend)) {
            findUser.removeFriend(idFriend);
            updateUser(findUser);
        } else {
            throw new NotFoundException(String.format("Friend ID=%d for user ID=%d", idFriend, idUser));
        }

        if (friendUser.getFriends().contains(idUser)) {
            friendUser.removeFriend(idUser);
            updateUser(friendUser);
        } else {
            throw new NotFoundException(String.format("Friend ID=%d for user ID=%d", idUser, idFriend));
        }

        if (findUserById(idUser).getFriends().contains(idFriend)) {
            log.error("Ошибка БД. У пользователя ID={} не удален друг ID={}", idUser, idFriend);
            throw new NotRemovedException(String.format("Friend ID=%d for user ID=%d", idFriend, idUser));
        } else {
            log.info("У Пользователя ID={} успешно удалён друг ID={}", idUser, idFriend);
        }

        if (findUserById(idFriend).getFriends().contains(idUser)) {
            log.error("Ошибка БД. У пользователя ID={} не удален друг ID={}", idFriend, idUser);
            throw new NotRemovedException(String.format("Friend ID=%d for user ID=%d", idUser, idFriend));
        } else {
            log.info("У Пользователя ID={} успешно удалён друг ID={}", idFriend, idUser);
        }
    }

    public List<User> getAllFriendsByUserId(int idUser) {
        User findUser = findUserById(idUser);
        log.info("В БД выполняется запрос на получение списка друзей пользователя ID={}", idUser);
        return findUser.getFriends().stream().map(this::findUserById).collect(Collectors.toList());
    }

    public List<User> getAllCommonFriendsByUserId(int idUser, int idFriend) {
        User findUser = findUserById(idUser);
        User findFriend = findUserById(idFriend);
        log.info("В БД выполняется запрос на получение списка общих друзей пользователей ID={} и ID={}",
                idUser, idFriend);

        Set<Integer> friendsUser = findUser.getFriends();
        Set<Integer> friendsUserFriend = findFriend.getFriends();

        Set<Integer> commonFriends = new HashSet<>(); //не смог собрать стрим, потом нужно переделать на стрим
        for (int id : friendsUser) {
            for (int id1 : friendsUserFriend) {
                if (id == id1) {
                    commonFriends.add(id1);
                }
            }
        }
        if (commonFriends.isEmpty()) {
            log.debug("Список общих друзей пользователя ID={} с пользователем ID={} пуст",
                    idUser, idFriend);
            return Collections.emptyList();
        } else {
            return commonFriends.stream().map(this::findUserById).collect(Collectors.toList());
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
