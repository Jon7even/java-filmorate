package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static javax.validation.Validation.buildDefaultValidatorFactory;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserControllerTest {
    private UserController userController;
    private Validator validator;
    private User userDefault;
    private User userNotName;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        ValidatorFactory factory = buildDefaultValidatorFactory();
        validator = factory.getValidator();
        initUsers();
    }

    void initUsers() {
        userDefault = new User(0, "yandex@yandex.ru", "userDefault", "UserTest",
                LocalDate.of(2000, 1, 1));
        userNotName = new User(1, "yandex1@yandex.ru", "userNotName", " ",
                LocalDate.of(1990, 4, 5));
    }

    @Test
    @DisplayName("Пользователь должен создаться с релевантными полями")
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
    @DisplayName("Пользователь должен обновить все поля")
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
    @DisplayName("Если поле email некорректно, валидатор должен сработать")
    void shouldBeValidationEmail() {
        userDefault.setEmail("yandexyandex.ru");
        Set<ConstraintViolation<User>> violations = validator.validate(userDefault);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage()
                        .equals("должно иметь формат адреса электронной почты")),
                "Email don't should empty");

        userNotName.setEmail("");
        violations = validator.validate(userNotName);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage().equals("не должно быть пустым")),
                "Email should have symbol @");
    }

    @Test
    @DisplayName("Если поле login некорректно, валидатор должен сработать")
    void shouldBeValidationLogin() {
        userDefault.setLogin("");
        Set<ConstraintViolation<User>> violations = validator.validate(userDefault);
        assertEquals(2, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage().equals("не должно быть пустым")),
                "Login don't should empty");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage()
                        .equals("размер должен находиться в диапазоне от 3 до 20")),
                "login should have 3-20 symbol");

        userNotName.setLogin(null);
        violations = validator.validate(userNotName);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage().equals("не должно быть пустым")),
                "Login don't be null");
    }

    @Test
    @DisplayName("Если поле name не указано, name должно быть равно login")
    void shouldBeValidationName() {
        userDefault.setName(null);
        userController.createUser(userDefault);
        userController.createUser(userNotName);
        List<User> userList = userController.getAllUsers();
        assertEquals(userDefault.getLogin(), userList.get(0).getName(), "Name must be login");
        assertEquals(userNotName.getLogin(), userList.get(1).getName(), "Name must be login");
    }

    @Test
    @DisplayName("Если поле birthday некорректно, валидатор должен сработать")
    void shouldBeValidationBirthday() {
        userDefault.setBirthday(LocalDate.of(2077, 7, 7));
        Set<ConstraintViolation<User>> violations = validator.validate(userDefault);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage().equals("должно содержать прошедшую дату")),
                "Birthday can't be later");

        userNotName.setBirthday(LocalDate.now());
        violations = validator.validate(userNotName);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage().equals("должно содержать прошедшую дату")),
                "Birthday can't be later");
    }

    @Test
    @DisplayName("Если пользователь с таким login уже есть в базе")
    void shouldThrowExceptionSameLoginUser() {
        userController.createUser(userDefault);
        userNotName.setLogin(userDefault.getLogin());
        final ValidationException exceptionSameLoginUser = assertThrows(
                ValidationException.class,
                () -> userController.createUser(userNotName));
        assertEquals("Пользователь с таким же логином уже имеется в системе - userDefault",
                exceptionSameLoginUser.getMessage());
    }

    @Test
    @DisplayName("Если пользователь с таким email уже есть в базе")
    void shouldThrowExceptionSameEmailAddAndPutUser() {
        userController.createUser(userDefault);
        userNotName.setEmail(userDefault.getEmail());
        final ValidationException exceptionSameEmailAddUser = assertThrows(
                ValidationException.class,
                () -> userController.createUser(userNotName));
        assertEquals("Пользователь с таким email - yandex@yandex.ru уже существует",
                exceptionSameEmailAddUser.getMessage());
        userNotName.setEmail("yandex@yandex.ru");
        final ValidationException exceptionSameEmailPutUser = assertThrows(
                ValidationException.class,
                () -> userController.updateUser(userNotName));
        assertEquals("Данный email - yandex@yandex.ru уже находится в БД",
                exceptionSameEmailPutUser.getMessage());
    }

    @Test
    @DisplayName("Если добавить запрещенные к регистрации логины")
    void shouldThrowExceptionAddLoginUserAdmin() {
        userDefault.setLogin("admin");
        final ValidationException exceptionAddLoginUserAdmin = assertThrows(
                ValidationException.class,
                () -> userController.createUser(userDefault));
        assertEquals("Регистрировать пользователя с такими именем запрещено - admin",
                exceptionAddLoginUserAdmin.getMessage());
    }

}
