package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.IdGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.constans.Settings.BAN_LIST_ADD_LOGIN;
import static ru.yandex.practicum.filmorate.constans.Settings.BAN_LIST_FIND_LOGIN;

@Slf4j
@Service
public class InMemoryUserStorage implements UserStorage {
    IdGenerator id;
    private final Map<Integer, User> users = new HashMap<>();

    @Autowired
    public InMemoryUserStorage(IdGenerator idGenerator) {
        this.id = idGenerator;
    }

    public List<User> getAllUsers() {
        log.info("Сделан запрос на получение списка всех пользователей. *Работает фильтр BanListFindLogin.properties");
        return users.values().stream().filter(user -> !BAN_LIST_FIND_LOGIN.contains(user.getLogin()))
                .collect(Collectors.toList());
    }

    public User createUser(User user) {
        if (isCheckLoginInBanList(user.getLogin())) {
            throw new ValidationException("Регистрировать пользователя с таким именем запрещено - " + user.getLogin());
        }
        if (isCheckLoginOnDuplicate(user.getLogin())) {
            throw new ValidationException("Пользователь с таким же логином уже имеется в системе - " + user.getLogin());
        }
        if (isCheckEmailInDateBase(user.getEmail())) {
            throw new ValidationException("Пользователь с таким email - " + user.getEmail() + " уже существует");
        }
        if (isCheckName(user)) {
            user.setName(user.getLogin());
        }
        user.setId(id.getIdGenerator());
        users.put(user.getId(), user);
        log.info("В БД успешно добавлен новый пользователь {}", users.get(user.getId()));
        return user;
    }

    public User updateUser(User user) {
        int userId = user.getId();
        if (users.containsKey(userId)) {
            if (isCheckLoginInBanList(user.getLogin())) {
                throw new ValidationException("Изменение на такой \"login\" - " + user.getLogin() + " запрещено");
            }
            if (isCheckEmailInDateBase(user.getEmail())) {
                throw new ValidationException("Данный email - " + user.getEmail() + " уже находится в БД");
            }
            User oldUser = users.get(userId);
            if (isCheckName(user)) {
                user.setName(user.getLogin());
            }
            users.put(userId, user);
            if (user.equals(oldUser)) {
                log.warn("При обновлении данных аккаунта, пользователь с ID={} не дал новых данных " +
                        "если это сообщение повторится, на это стоит обратить внимание", userId);
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
        return users.values().stream().anyMatch(user -> user.getEmail().equalsIgnoreCase(emailCheck));
    }

    private Boolean isCheckLoginInBanList(String login) {
        return BAN_LIST_ADD_LOGIN.stream().anyMatch(login::equalsIgnoreCase);
    }

    private Boolean isCheckLoginOnDuplicate(String login) {
        return users.values().stream().anyMatch(user -> user.getLogin().equalsIgnoreCase(login));
    }

    public void clearRepository() {
        id.resetIdGenerator();
        users.clear();
    }
}
