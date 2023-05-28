package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserRelation;
import ru.yandex.practicum.filmorate.model.UserRelationStatus;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.constants.NameLogs.SERVICE_FROM_DB;
import static ru.yandex.practicum.filmorate.constants.NameLogs.SERVICE_IN_DB;
import static ru.yandex.practicum.filmorate.model.UserRelationStatus.*;
import static ru.yandex.practicum.filmorate.utils.BanListUserName.BAN_LIST_ADD_LOGIN;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        log.debug("{} на получение списка всех пользователей", SERVICE_IN_DB);
        List<User> listUser = userStorage.getAllUsers();

        if (listUser.isEmpty()) {
            log.info("{} пустой список пользователей", SERVICE_FROM_DB);
        } else {
            log.info("{} успешно список из [count={}] пользователей", SERVICE_FROM_DB, listUser.size());
        }
        return listUser;
    }

    public User findUserById(Integer idUser) {
        userNotFoundByIdCheckPositive(idUser);
        log.debug("{} на получение пользователя [ID={}]", SERVICE_IN_DB, idUser);
        Optional<User> getUser = userStorage.findUserById(idUser);

        if (getUser.isPresent()) {
            log.info("{} успешно пользователь с [ID={}]", SERVICE_FROM_DB, idUser);
        } else {
            log.error("{} [User is empty] пользователя с [ID={}] не существует", SERVICE_FROM_DB, idUser);
            userNotFoundByIdException(idUser);
        }
        return getUser.get();
    }

    public User updateUser(User user) {
        int idUser = user.getId();
        User checkFoundUser = checkExistUser(idUser);

        if (isCheckLoginInBanList(user.getLogin())) {
            throw new ValidationException(Collections.singleton(Map.of("login",
                    String.format("Изменение логина на [%s] запрещено", user.getLogin()))));
        }
        if (isCheckName(user)) {
            user.setName(user.getLogin());
        }

        if (checkFoundUser.equals(user)) {
            log.info("{} пользователь [login={}], но новых данных для обновления пользователя нет", SERVICE_FROM_DB,
                    checkFoundUser.getLogin());
            log.debug("Данные из контроллера: [User={}],\n Данные из БД: [User={}]", user, checkFoundUser);
            return checkFoundUser;
        }

        log.debug("{} на обновление данных пользователя с [ID={}]", SERVICE_IN_DB, idUser);
        Optional<User> updatedUser = userStorage.updateUser(user);

        if (updatedUser.isEmpty()) {
            log.error("{} [User is empty]. " +
                            "По неизвестной причине не получилось обновить данные пользователя [login={}]",
                    SERVICE_FROM_DB, user.getLogin());
            userNotFoundByIdException(idUser);
        } else {
            log.info("{} успешно обновленный пользователь [login={}]", SERVICE_FROM_DB, updatedUser.get().getLogin());
            log.debug("Данные до обновления: \n[User={}], Данные после обновления: \n[User={}]",
                    checkFoundUser, updatedUser.get());
        }
        return updatedUser.get();
    }

    public User createUser(User user) {
        if (isCheckLoginInBanList(user.getLogin())) {
            throw new ValidationException(Collections.singleton(Map.of("login",
                    String.format("Регистрировать пользователя с таким именем [%s] запрещено", user.getLogin()))));
        }
        if (isCheckName(user)) {
            user.setName(user.getLogin());
        }
        log.debug("{} на добавление нового пользователя", SERVICE_IN_DB);
        Optional<User> createdUser = userStorage.createUser(user);

        if (createdUser.isPresent()) {
            log.info("{} успешно новый пользователь [{}]", SERVICE_FROM_DB, createdUser.get().getLogin());
        } else {
            log.error("{} [User is empty]. " +
                    "По неизвестной причине не получилось добавить нового пользователя", SERVICE_FROM_DB);
            throw new NotCreatedException("New user");
        }
        return createdUser.get();
    }

    public void addFriend(Integer idUser, Integer idFriend) {
        log.info("{} на добавление в друзья пользователя [ID={}] к пользователю [ID={}]",
                SERVICE_IN_DB, idFriend, idUser);
        User getUser = checkExistUser(idUser);
        User getFriend = checkExistUser(idFriend);
        UserRelationStatus userRelationStatus = userStorage.addFriend(idUser, idFriend);

        switch (userRelationStatus) {
            case NO_RELATION:
                log.error("{} пользователь [login={}] но друг [login={}] не добавлен в БД",
                        SERVICE_FROM_DB, getUser.getLogin(), getFriend.getLogin());
                throw new NotCreatedException(String.format("New friend for user ID=%d", idUser));

            case REQUEST:
                log.info("{} успешно обновленный [login={}] пользователь: отправлена заявка в друзья пользователю " +
                        "[login={}]", SERVICE_FROM_DB, getUser.getLogin(), getFriend.getLogin());
                log.debug("Текущий статус дружбы: {}", REQUEST);
                break;

            case APPROVED:
                log.info("{} успешно обновленный [login={}] пользователь: дружба подтверждена с пользователем " +
                        "[login={}]", SERVICE_FROM_DB, getUser.getLogin(), getFriend.getLogin());
                log.debug("Текущий статус дружбы: {}", APPROVED);
                break;

            case BLACK_LIST:
                log.info("{} успешно обновленный [login={}] пользователь: добавлен в черный список пользователь " +
                        "[login={}]", SERVICE_FROM_DB, getUser.getLogin(), getFriend.getLogin());
                log.debug("Текущий статус дружбы: {}", BLACK_LIST);
                break;

            default:
                log.error("В БД произошла неизвестная ошибка связанная со статусом дружбы пользователей");
                throw new UnknownException("Status Friendship");
        }
    }

    public void removeFriend(Integer idUser, Integer idFriend) {
        log.debug("{} на удаление из друзей пользователя [ID={}] у пользователя [ID={}]",
                SERVICE_IN_DB, idFriend, idUser);
        User getUser = checkExistUser(idUser);
        User getFriend = checkExistUser(idFriend);
        UserRelationStatus userRelationStatus = userStorage.removeFriend(idUser, idFriend);

        switch (userRelationStatus) {
            case NO_RELATION:
                log.info("{} успешно обновленный [login={}] пользователь: удалена заявка в друзья пользователю " +
                        "[login={}]", SERVICE_FROM_DB, getUser.getLogin(), getFriend.getLogin());
                log.debug("Текущий статус дружбы: {}", REQUEST);
                break;
            case REQUEST:
                log.error("{} пользователь [login={}] но заявка в друзья пользователю [login={}] не была удалена",
                        SERVICE_FROM_DB, getUser.getLogin(), getFriend.getLogin());
                throw new NotRemovedException(String.format("Friend ID=%d for user ID=%d", idFriend, idUser));

            case APPROVED:
                log.error("{} пользователь [login={}] но друг [login={}] не был удален",
                        SERVICE_FROM_DB, getUser.getLogin(), getFriend.getLogin());
                throw new NotRemovedException(String.format("Friend ID=%d for user ID=%d", idFriend, idUser));

            case BLACK_LIST:
                log.debug("Текущий статус дружбы: {}", BLACK_LIST);
                throw new NotRemovedException(String.format("Friend ID=%d for user ID=%d", idFriend, idUser));

            default:
                log.error("В БД произошла неизвестная ошибка связанная со статусом дружбы пользователей");
                throw new UnknownException("Status Friendship");
        }
    }

    public List<User> getAllFriendsByUserId(Integer idUser) {
        log.info("{} на получение списка друзей пользователя [ID={}]", SERVICE_IN_DB, idUser);
        UserRelation user = new UserRelation(checkExistUser(idUser));
        UserRelation userAndFriends = userStorage.getAllFriendsByUserId(user);

        if (userAndFriends.getFriends().isEmpty()) {
            log.info("{} пустой список друзей пользователя [ID={}]", SERVICE_FROM_DB, idUser);
        } else {
            log.info("{} успешно список из [count={}] друзей пользователя [login={}]",
                    SERVICE_FROM_DB, userAndFriends.getFriends().size(), userAndFriends.getUser().getLogin());
        }
        return userAndFriends.sortedListUser();
    }

    public List<User> getAllCommonFriendsByUserId(Integer idUser, Integer idFriend) {
        log.info("{} на получение общего списка друзей пользователя [ID={}] " +
                "с пользователем [ID={}]", SERVICE_IN_DB, idUser, idFriend);
        UserRelation userRelation = new UserRelation(checkExistUser(idUser));
        UserRelation friendRelation = new UserRelation(checkExistUser(idFriend));
        List<User> listAllCommonFriendsByUserId = userStorage.getAllCommonFriendsByUserId(userRelation, friendRelation);

        if (listAllCommonFriendsByUserId.isEmpty()) {
            log.info("{} пустой список общих друзей пользователя [ID={}] с пользователем [ID={}]",
                    SERVICE_FROM_DB, idUser, idFriend);
        } else {
            log.info("{} успешно список из [count={}] общих друзей пользователя [ID={}] и [ID={}]",
                    SERVICE_FROM_DB, listAllCommonFriendsByUserId.size(), idUser, idFriend);
        }
        return listAllCommonFriendsByUserId;
    }

    private User checkExistUser(Integer idUser) {
        userNotFoundByIdCheckPositive(idUser);
        log.debug("{} на проверку пользователя с [ID={}]", SERVICE_IN_DB, idUser);
        Optional<User> checkFoundUser = userStorage.findUserById(idUser);
        if (checkFoundUser.isEmpty()) {
            userNotFoundByIdException(idUser);
        }
        return checkFoundUser.get();
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

    private Boolean isCheckName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Пользователь не указал имени. Поле [name] берется из логина - [login={}]", user.getLogin());
            return true;
        }
        return false;
    }
}
