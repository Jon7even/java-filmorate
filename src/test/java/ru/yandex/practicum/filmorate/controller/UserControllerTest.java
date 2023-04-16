package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class UserControllerTest {

    private UserController userController;

    private User userDefault;
    private User userNotName;
    private User userBadField;


    @BeforeEach
    void setUp() {
        userController = new UserController();
        initUsers();
    }

    void initUsers() {
        userDefault = new User(1, "yandex@yandex.ru", "userDefault", "UserTest",
                LocalDate.of(2000, 1, 1));
        userNotName = new User(2, "yandex1@yandex.ru", "userNotName", "",
                LocalDate.of(1990, 4, 5));
        userBadField = new User(-1, "yandex@yandex.ru", "1", "",
                LocalDate.of(2077, 7, 7));
    }

    @Test
    void shouldCreateUser() {
        userController.createUser(userDefault);
        List<User> userList = userController.getAllUsers();
        assertEquals(1, userList.size(), "Quantity users must be 1");
        assertEquals(userDefault.getId(), userList.get(0).getId(), "ID User must be 1");
        assertEquals(userDefault.getEmail(), userList.get(0).getEmail(), "Email should equals");
        assertEquals(userDefault.getLogin(), userList.get(0).getLogin(), "Login should equals");
        assertEquals(userDefault.getName(), userList.get(0).getName(), "Name should equals");
        assertEquals(userDefault.getBirthday(), userList.get(0).getBirthday(), "Birthday should equals");
    }
}
