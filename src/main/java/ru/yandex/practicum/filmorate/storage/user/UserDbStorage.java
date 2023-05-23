package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotCreatedException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.constans.NameLogs.DB_RUNNING;
import static ru.yandex.practicum.filmorate.constans.NameLogs.DB_SUCCESS;
import static ru.yandex.practicum.filmorate.utils.BanListUserName.BAN_LIST_FIND_LOGIN;

@Slf4j
@Component
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findUserById(Integer idUser) {
        log.debug("{} на получение данных пользователя [ID={}]", DB_RUNNING, idUser);
        String sql = "SELECT * FROM PERSON WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper(), idUser);
        int countUsers = users.size();

        if (countUsers > 1) {
            log.error("Ожидался список из 1 пользователя, а получился [COUNT={}]", countUsers);
            throw new NotFoundException(String.format("User with ID=%d", idUser));
        } else if (countUsers < 1) {
            log.error("Пользователь с [ID={}] не найден", idUser);
            throw new NotFoundException(String.format("User with ID=%d", idUser));
        } else {
            return Optional.of(users.get(0));
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
        String sql = "INSERT INTO person (email, login, first_name, BIRTHDAY) VALUES(?, ?, ?, ?)";

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, user.getEmail());
                statement.setString(2, user.getLogin());
                statement.setString(3, user.getName());
                statement.setDate(4, Date.valueOf(user.getBirthday()));
                return statement;
            }, keyHolder);

            user.setId(keyHolder.getKey().intValue());
            log.debug("{} новый пользователь {}", DB_SUCCESS, user);
            return Optional.of(user);
        } catch (NullPointerException e) {
            log.error(e.getMessage());
            throw new NotCreatedException("New user");
        }
    }

    public List<User> getAllUsers() {
        log.debug("{} на получение списка всех пользователей. " +
                "*Работает фильтр BanListFindLogin.properties", DB_RUNNING);
        String sql = "SELECT * FROM person";
        return jdbcTemplate.queryForStream(sql, userRowMapper())
                .filter(user -> !BAN_LIST_FIND_LOGIN.contains(user.getLogin()))
                .collect(Collectors.toList());
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
