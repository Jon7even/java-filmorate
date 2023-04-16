package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserControllerTest {

    private UserController userController;
    private User userDefault;
    private User userNotName;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        initUsers();
    }

    void initUsers() {
        userDefault = new User(0, "yandex@yandex.ru", "userDefault", "UserTest",
                LocalDate.of(2000, 1, 1));
        userNotName = new User(1, "yandex1@yandex.ru", "userNotName", " ",
                LocalDate.of(1990, 4, 5));
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

    @Test
    void shouldPutUser() {
        userController.createUser(userDefault);
        userController.updateUser(userNotName);
        List<User> userList = userController.getAllUsers();
        assertEquals(1, userList.size(), "Quantity users must be 1");
        assertEquals(userNotName.getId(), userList.get(0).getId(), "ID User must be 1");
        assertEquals(userNotName.getEmail(), userList.get(0).getEmail(), "Email should equals");
        assertEquals(userNotName.getLogin(), userList.get(0).getLogin(), "Login should equals");
        assertEquals(userNotName.getName(), userList.get(0).getName(), "Name should equals");
        assertEquals(userNotName.getBirthday(), userList.get(0).getBirthday(), "Birthday should equals");
    }

    @Test
    void shouldBeValidationEmail() {
        userDefault.setEmail("");
        userController.createUser(userDefault);
        List<User> userList1 = userController.getAllUsers();
        assertEquals(0, userList1.size(), "Email don't should empty");
        userDefault.setEmail("yandexyandex.ru");
        userController.createUser(userDefault);
        List<User> userList2 = userController.getAllUsers();
        assertEquals(0, userList2.size(), "Email should have symbol @");
    }

    @Test
    void shouldBeValidationLogin() {
        userDefault.setLogin(" ");
        userController.createUser(userDefault);
        List<User> userList1 = userController.getAllUsers();
        assertEquals(0, userList1.size(), "Login don't should empty");
        userDefault.setLogin(null);
        userController.createUser(userDefault);
        List<User> userList2 = userController.getAllUsers();
        assertEquals(0, userList2.size(), "Login don't be null");
    }

    @Test
    void shouldBeValidationName() {
        userController.createUser(userNotName);
        List<User> userList1 = userController.getAllUsers();
        assertEquals("userNotName", userList1.get(0).getName(), "Name must be login");
        userDefault.setName(null);
        userController.createUser(userDefault);
        List<User> userList2 = userController.getAllUsers();
        assertEquals("userDefault", userList2.get(1).getName(), "Name must be login");
    }

    @Test
    void shouldBeValidationBirthday() {
        userDefault.setBirthday(LocalDate.of(2077, 7, 7));
        userController.createUser(userDefault);
        List<User> userList = userController.getAllUsers();
        assertEquals(0, userList.size(), "Birthday can't be later");
    }

    @Test
    void shouldThrowExceptionSameLoginUser() {
        userController.createUser(userDefault);
        userNotName.setLogin(userDefault.getLogin());
        final ValidationException exceptionSameLoginUser = assertThrows(
                ValidationException.class,
                () -> {
                    userController.createUser(userNotName);
                });
        assertEquals("Пользователь с таким же логином уже имеется в системе - userDefault",
                exceptionSameLoginUser.getMessage());
    }

    @Test
    void shouldThrowExceptionSameEmailAddAndPutUser() {
        userController.createUser(userDefault);
        userNotName.setEmail(userDefault.getEmail());
        final ValidationException exceptionSameEmailAddUser = assertThrows(
                ValidationException.class,
                () -> {
                    userController.createUser(userNotName);
                });
        assertEquals("Пользователь с таким email - yandex@yandex.ru уже существует",
                exceptionSameEmailAddUser.getMessage());
        userNotName.setEmail("yandex@yandex.ru");
        final ValidationException exceptionSameEmailPutUser = assertThrows(
                ValidationException.class,
                () -> {
                    userController.updateUser(userNotName);
                });
        assertEquals("Данный email - yandex@yandex.ru уже находится в БД",
                exceptionSameEmailPutUser.getMessage());
    }

    @Test
    void shouldThrowExceptionAddLoginUserAdmin() {
        userDefault.setLogin("admin");
        final ValidationException exceptionAddLoginUserAdmin = assertThrows(
                ValidationException.class,
                () -> {
                    userController.createUser(userDefault);
                });
        assertEquals("Регистрировать пользователя с такими именем запрещено - admin",
                exceptionAddLoginUserAdmin.getMessage());
    }

}
