package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.IdGenerator;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.constans.Settings.BAN_LIST_FIND_LOGIN;

@Slf4j
@Service
public class InMemoryUserStorage implements UserStorage {
    private final IdGenerator id;
    private final Map<Integer, User> users;

    public InMemoryUserStorage() {
        this.id = new IdGenerator();
        this.users = new HashMap<>();
    }

    public List<User> getAllUsers() {
        log.debug("В БД выполняется запрос на получение списка всех пользователей. " +
                "*Работает фильтр BanListFindLogin.properties");
        return users.values().stream().filter(user -> !BAN_LIST_FIND_LOGIN.contains(user.getLogin()))
                .collect(Collectors.toList());
    }

    public User createUser(User user) {
        log.debug("В БД выполняется запрос на добавление нового пользователя");

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
        log.debug("В БД добавлен новый пользователь {}", createdUser);
        return createdUser;
    }

    public User updateUser(User user) {
        int idUser = user.getId();
        log.debug("В БД выполняется запрос на обновление данных пользователя [ID={}]", idUser);

        if (users.containsKey(idUser)) {
            if (isCheckEmailInDateBase(user.getEmail())) {
                log.warn("Пользователь сменил [email={}] но он уже есть в БД", user.getEmail());
            }
            User oldUser = users.get(idUser);

            if (isCheckName(user)) {
                user.setName(user.getLogin());
            }
            users.put(idUser, user);
            User updateUser = users.get(idUser);

            if (updateUser.equals(oldUser)) {
                log.warn("При обновлении данных аккаунта, пользователь с [ID={}] не дал новых данных " +
                        "если это сообщение повторится, на это стоит обратить внимание", idUser);
            }

            log.debug("Пользователь с [ID={}] успешно обновлен!\n Старый аккаунт: {},\n Новый аккаунт: {}",
                    idUser, oldUser, updateUser);
            return updateUser;
        } else {
            throw new NotFoundException(String.format("User with ID=%d", idUser));
        }
    }

    public User findUserById(int idUser) {
        log.debug("В БД выполняется запрос на получение данных пользователя [ID={}]", idUser);
        if (users.containsKey(idUser)) {
            return users.get(idUser);
        } else {
            throw new NotFoundException(String.format("User with ID=%d", idUser));
        }
    }

    public User addFriend(int idUser, int idFriend) {
        log.debug("В БД выполняется запрос на добавление друга [ID={}] пользователю [ID={}]", idFriend, idUser);
        User getUser = findUserById(idUser);
        if (getUser.getFriends().contains(idFriend)) {
            log.error("У пользователя [ID={}] уже есть друг [ID={}]", idUser, idFriend);
            throw new AlreadyExistsException(String.format("Friend with ID=%d", idFriend));
        }
        getUser.addFriend(idFriend);
        updateUser(getUser);

        User addedFriendForUser = findUserById(idUser);
        if (addedFriendForUser.getFriends().contains(idFriend)) {
            log.debug("В БД пользователю [ID={}] успешно добавлен друг [ID={}]", idUser, idFriend);
            return addedFriendForUser;
        } else {
            log.error("Ошибка БД. Пользователю [ID={}] не добавлен друг [ID={}]", idUser, idFriend);
            throw new NotCreatedException(String.format("New friend for user ID=%d", idUser));
        }
    }

    public User removeFriend(int idUser, int idFriend) {
        log.debug("В БД выполняется запрос на удаление друга [ID={}] у пользователя [ID={}]", idFriend, idUser);
        User getUser = findUserById(idUser);

        if (getUser.getFriends().contains(idFriend)) {
            getUser.removeFriend(idFriend);
            updateUser(getUser);
        } else {
            throw new NotFoundException(String.format("Friend ID=%d for user ID=%d", idFriend, idUser));
        }

        User removedFriendForUser = findUserById(idUser);

        if (removedFriendForUser.getFriends().contains(idFriend)) {
            log.error("Ошибка БД. У пользователя [ID={}] не удален друг [ID={}]", idUser, idFriend);
            throw new NotRemovedException(String.format("Friend ID=%d for user ID=%d", idFriend, idUser));
        } else {
            log.debug("В БД у пользователя [ID={}] успешно удалён друг [ID={}]", idUser, idFriend);
            User friendUser = findUserById(idFriend);
            if(friendUser.getFriends().contains(idUser)) {
                log.debug("Но друг [ID={}] еще не удалил пользователя [ID={}]", idFriend, idUser);
            }
            return removedFriendForUser;
        }
    }

    public List<User> getAllFriendsByUserId(int idUser) {
        User getUser = findUserById(idUser);
        log.debug("В БД выполняется запрос на получение списка друзей пользователя [ID={}]", idUser);
        return getUser.getFriends().stream().map(this::findUserById).collect(Collectors.toList());
    }

    public List<User> getAllCommonFriendsByUserId(int idUser, int idFriend) {
        User findUser = findUserById(idUser);
        User findFriend = findUserById(idFriend);
        log.debug("В БД выполняется запрос на получение списка общих друзей пользователей [ID={}] и [ID={}]",
                idUser, idFriend);

        Set<Integer> commonFriends = new HashSet<>(findUser.getFriends());
        commonFriends.retainAll(findFriend.getFriends());

        if (commonFriends.isEmpty()) {
            log.debug("Список общих друзей пользователя [ID={}] с пользователем [ID={}] пуст",
                    idUser, idFriend);
            return Collections.emptyList();
        } else {
            return commonFriends.stream().map(this::findUserById).collect(Collectors.toList());
        }
    }

    private Boolean isCheckName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Пользователь не указал имени. Поле [name] берется из логина - [login={}]", user.getLogin());
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
