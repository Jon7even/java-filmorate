package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserRelation;
import ru.yandex.practicum.filmorate.model.UserRelationStatus;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.constans.NameLogs.DB_RUNNING;
import static ru.yandex.practicum.filmorate.constans.NameLogs.DB_SUCCESS;
import static ru.yandex.practicum.filmorate.model.UserRelationStatus.*;
import static ru.yandex.practicum.filmorate.utils.BanListUserName.BAN_LIST_FIND_LOGIN;

@Slf4j
@Component
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<User> getAllUsers() {
        log.debug("{} на получение списка всех пользователей. " +
                "*Работает фильтр BanListFindLogin.properties", DB_RUNNING);
        String sql = "SELECT * " +
                "  FROM person";
        return jdbcTemplate.queryForStream(sql, userRowMapper())
                .filter(user -> !BAN_LIST_FIND_LOGIN.contains(user.getLogin()))
                .collect(Collectors.toList());
    }

    public Optional<User> findUserById(Integer idUser) {
        log.debug("{} на получение данных пользователя [ID={}]", DB_RUNNING, idUser);
        String sql = "SELECT * " +
                "  FROM PERSON " +
                " WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper(), idUser);
        int countUsers = users.size();

        if (countUsers > 1) {
            log.error("Ожидался список из 1 пользователя, а получился [COUNT={}]", countUsers);
            throw new NotFoundException(String.format("User with ID=%d", idUser));
        } else if (countUsers < 1) {
            log.warn("Пользователь с [ID={}] не найден", idUser);
            throw new NotFoundException(String.format("User with ID=%d", idUser));
        } else {
            return Optional.ofNullable(users.get(0));
        }
    }

    public Optional<User> updateUser(User user) {
        int idUpdateUser = user.getId();
        log.debug("{} на обновление данных пользователя [ID={}]", DB_RUNNING, idUpdateUser);

        if (isCheckLoginOnDuplicate(user.getLogin())) {
            throw new ValidationException(Collections.singleton(Map.of("login",
                    String.format("Пользователь с таким логином [%s] уже имеется в системе", user.getLogin()))));
        }
        if (isCheckEmailInDateBase(user.getEmail())) {
            log.warn("Пользователь сменил [email={}] но он уже есть в БД", user.getEmail());
        }
        String sql = "UPDATE person " +
                "   SET " +
                "       email = ?, " +
                "       login = ?, " +
                "       first_name = ?, " +
                "       birthday = ? " +
                " WHERE id = ?";
        try {
            jdbcTemplate.update(sql,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday(),
                    user.getId()
            );
            log.debug("{} обновлен пользователь [ID={}]", DB_SUCCESS, idUpdateUser);
            return findUserById(idUpdateUser);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new NotUpdatedException(String.format("User with [ID=%d]", idUpdateUser));
        }
    }

    public Optional<User> createUser(User user) {
        log.debug("{} на добавление нового пользователя", DB_RUNNING);

        if (isCheckLoginOnDuplicate(user.getLogin())) {
            throw new ValidationException(Collections.singleton(Map.of("login",
                    String.format("Пользователь с таким логином [%s] уже имеется в системе", user.getLogin()))));
        }
        if (isCheckEmailInDateBase(user.getEmail())) {
            throw new ValidationException(Collections.singleton(Map.of("login",
                    String.format("Пользователь с таким email [%s] уже имеется в системе", user.getEmail()))));
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO person (email, login, first_name, BIRTHDAY) " +
                "     VALUES(?, ?, ?, ?)";

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, user.getEmail());
                statement.setString(2, user.getLogin());
                statement.setString(3, user.getName());
                statement.setDate(4, Date.valueOf(user.getBirthday())
                );
                return statement;
            }, keyHolder);

            int newUserId = keyHolder.getKey().intValue();
            log.debug("{} добавлен новый пользователь [ID={}]", DB_SUCCESS, newUserId);
            return findUserById(newUserId);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new NotCreatedException("New user");
        }
    }

    private UserRelationStatus getUserRelationStatus(Integer idUser, Integer idFriend) {
        log.debug("{} проверки текущего статуса дружбы у пользователя [ID={}] с пользователем [ID={}]",
                DB_RUNNING, idUser, idFriend);
        String sql = "SELECT friendship FROM person_friend WHERE person_id = ? AND person_friend_id = ?";
        try {
            List<String> getQueryString = jdbcTemplate.queryForList(sql, String.class, idUser, idFriend);
            int resultCount = getQueryString.size();

            if (resultCount > 1) {
                log.error("Ожидался [COUNT=1] статус дружбы между пользователями, а получилось [COUNT={}]",
                        resultCount);
                throw new UnknownException("Status Friendship");
            } else if (resultCount < 1) {
                log.debug("Статус еще не выставлен");
                return NO_RELATION;
            } else {
                return valueOf(getQueryString.get(0));
            }
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new UnknownException("Status Friendship");
        }
    }

    public UserRelationStatus addFriend(Integer idUser, Integer idFriend) {
        log.debug("{} на добавление друга [ID={}] пользователю [ID={}]", DB_RUNNING, idFriend, idUser);
        UserRelationStatus userRelationStatus = getUserRelationStatus(idUser, idFriend);

        switch (userRelationStatus) {
            case NO_RELATION:
                try {
                    String sqlInsert = "INSERT INTO person_friend (person_id, person_friend_id, friendship) " +
                            "     VALUES(?, ?, ?)";
                    jdbcTemplate.update(sqlInsert, idUser, idFriend, REQUEST.toString());
                    log.debug("В БД выполнен запрос на добавление друга [ID={}] пользователю [ID={}]",
                            idFriend, idUser);

                    if (getUserRelationStatus(idFriend, idUser).equals(REQUEST)) {
                        String sqlUpdate = "UPDATE person_friend " +
                                "   SET " +
                                "       friendship = ? " +
                                " WHERE person_id IN (?,?)";
                        jdbcTemplate.update(sqlUpdate, idUser, idFriend, APPROVED.toString());
                        log.debug("В БД выполнен запрос на обновление статусов дружбы у пользователей " +
                                "[ID={}] и [ID={}]", idFriend, idUser);
                    }
                    return getUserRelationStatus(idUser, idFriend);
                } catch (DataAccessException e) {
                    log.error(e.getMessage());
                    log.error("Ошибка БД. Пользователю [ID={}] не добавлен друг [ID={}]", idUser, idFriend);
                    throw new NotCreatedException(String.format("New friend for user ID=%d", idUser));
                }

            case REQUEST:
                log.info("Пользователь [ID={}] уже отправлял заявку в друзья пользователю [ID={}]", idUser, idFriend);
                throw new AlreadyExistsException(String.format("Friend with ID=%d", idFriend));

            case APPROVED:
                log.info("Пользователь [ID={}] уже имеет статус подтвержденной дружбы с пользователем [ID={}]",
                        idUser, idFriend);
                throw new AlreadyExistsException(String.format("Friend with ID=%d", idFriend));

            case BLACK_LIST:
                log.error("Функция поместить пользователя в черный список еще не реализована.");
                throw new AlreadyExistsException(String.format("Friend with ID=%d", idFriend));

            default:
                log.error("В БД произошла неизвестная ошибка связанная со статусом дружбы пользователей");
                throw new UnknownException("Status Friendship");
        }
    }

    public UserRelationStatus removeFriend(Integer idUser, Integer idFriend) {
        log.debug("{} на удаление друга [ID={}] у пользователя [ID={}]", DB_RUNNING, idFriend, idUser);

        UserRelationStatus userRelationStatus = getUserRelationStatus(idUser, idFriend);

        switch (userRelationStatus) {
            case NO_RELATION:
                log.error("У пользователя [ID={}] уже удален друг [ID={}]", idUser, idFriend);
                throw new NotRemovedException(String.format("Friend ID=%d for user ID=%d", idFriend, idUser));

            case REQUEST:
                try {
                    String sqlDelete = "DELETE FROM person_friend WHERE person_id = ? AND person_friend_id = ?";
                    jdbcTemplate.update(sqlDelete, idUser, idFriend);
                    log.debug("В БД выполнен запрос на удаление заявки в друзья пользователя " +
                            "[ID={}] у пользователя [ID={}]", idFriend, idUser);
                    return getUserRelationStatus(idUser, idFriend);
                } catch (DataAccessException e) {
                    log.error("Ошибка БД. У пользователя [ID={}] не удален друг [ID={}]", idUser, idFriend);
                    throw new NotRemovedException(String.format("Friend ID=%d for user ID=%d", idFriend, idUser));
                }

            case APPROVED:
                try {
                    String sqlDelete = "DELETE FROM person_friend WHERE person_id = ? AND person_friend_id = ?";
                    jdbcTemplate.update(sqlDelete, idUser, idFriend);
                    log.debug("В БД выполнен запрос на удаление из друзей пользователя [ID={}] у пользователя [ID={}]",
                            idFriend, idUser);

                    String sqlUpdate = "UPDATE person_friend " +
                            "   SET " +
                            "       friendship = ? " +
                            " WHERE person_id = ?)";
                    jdbcTemplate.update(sqlUpdate, REQUEST.toString(), idFriend);
                    log.debug("В БД выполнен запрос на обновление статуса дружбы у [ID={}] с пользователем [ID={}]",
                            idFriend, idUser);
                    return getUserRelationStatus(idUser, idFriend);
                } catch (DataAccessException e) {
                    log.error("Ошибка БД. У пользователя [ID={}] не удален друг [ID={}]", idUser, idFriend);
                    throw new NotRemovedException(String.format("Friend ID=%d for user ID=%d", idFriend, idUser));
                }

            case BLACK_LIST:
                log.error("Функция удалить пользователя из черного списка еще не реализована.");
                throw new AlreadyExistsException(String.format("Friend with ID=%d", idFriend));

            default:
                log.error("В БД произошла неизвестная ошибка связанная со статусом дружбы пользователей");
                throw new UnknownException("Status Friendship");
        }
    }

    public UserRelation getAllFriendsByUserId(UserRelation user) {
        log.debug("{} на получение списка друзей пользователя [ID={}] [login={}]",
                DB_RUNNING, user.getUser().getId(), user.getUser().getLogin());
        String sqlSelectIdUserFriends = "SELECT person_friend_id " +
                "  FROM person_friend" +
                " WHERE person_id = ?";
        try {
            List<Integer> getQueryInteger = jdbcTemplate.queryForList(sqlSelectIdUserFriends,
                    Integer.class, user.getUser().getId());
            int resultCount = getQueryInteger.size();

            if (resultCount < 1) {
                log.debug("Друзья у пользователя [login={}] не найдены", user.getUser().getLogin());
            } else {
                user.setFriends(getQueryInteger.stream()
                        .map(this::findUserById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet()));
            }
            return user;
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new UnknownException("Get Friends for User");
        }
    }

    public List<User> getAllCommonFriendsByUserId(UserRelation userRelation, UserRelation friendRelation) {
        log.debug("{} на получение списка общих друзей пользователей [ID={}] [login={}] и [ID={}] [login={}]",
                DB_RUNNING, userRelation.getUser().getId(), userRelation.getUser().getLogin(),
                friendRelation.getUser().getId(), friendRelation.getUser().getLogin());

        Set<User> commonFriends = new HashSet<>(getAllFriendsByUserId(userRelation).getFriends());
        commonFriends.retainAll(getAllFriendsByUserId(friendRelation).getFriends());

        if (commonFriends.isEmpty()) {
            log.debug("Список общих друзей пользователя [ID={}] с пользователем [ID={}] пуст",
                    userRelation.getUser().getId(), friendRelation.getUser().getId());
            return Collections.emptyList();
        } else {
            return new ArrayList<>(commonFriends);
        }
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("first_name"),
                rs.getDate("birthday").toLocalDate());
    }

    private Boolean isCheckEmailInDateBase(String emailCheck) {
        return false; //users.values().stream().anyMatch(user -> user.getEmail().equalsIgnoreCase(emailCheck));
    }

    private Boolean isCheckLoginOnDuplicate(String login) {
        return false; // users.values().stream().anyMatch(user -> user.getLogin().equalsIgnoreCase(login));
    }
}
