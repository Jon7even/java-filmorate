package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static javax.validation.Validation.buildDefaultValidatorFactory;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private InMemoryUserStorage inMemoryUserStorage;

    private Validator validator;
    private User userDefault;
    private User userDefault1;
    private User userNotName;

    @BeforeEach
    void setUp() {
        inMemoryUserStorage.clearRepository();
        ValidatorFactory factory = buildDefaultValidatorFactory();
        validator = factory.getValidator();
        initUsers();
    }

    void initUsers() {
        userDefault = new User(0, "yandex@yandex.ru", "userDefault", "UserTest",
                LocalDate.of(2000, 1, 1));
        userDefault1 = new User(1, "yandex1@yandex.ru", "userDefault1", "UserTest1",
                LocalDate.of(2000, 1, 2));
        userNotName = new User(0, "yandex1@yandex.ru", "userNotName", " ",
                LocalDate.of(1990, 4, 5));
    }

    @Test
    @DisplayName("Пользователь должен создаться с релевантными полями")
    void shouldCreateUser_thenStatus201() throws Exception {
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(userDefault))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("yandex@yandex.ru"))
                .andExpect(MockMvcResultMatchers.jsonPath("login").value("userDefault"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("UserTest"))
                .andExpect(MockMvcResultMatchers.jsonPath("birthday").value("2000-01-01"));
    }

    @Test
    @DisplayName("Поиск пользователя по [ID]")
    void shouldGetUser_thenById() throws Exception {
        long idUser = userService.createUser(userDefault).getId();
        mockMvc.perform(get("/users/{id}", idUser))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(idUser))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("UserTest"));
        mockMvc.perform(get("/users/{id}", -1))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/users/{id}", idUser + 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Endpoint [friends] and [common friends]")
    void shouldRun_Friends() throws Exception {
        long idUser1 = userService.createUser(userDefault).getId();
        long idUser2 = userService.createUser(userNotName).getId();
        userDefault1.setLogin("myFriend");
        userDefault1.setEmail("yandex2@yandex.ru");
        long idUser3 = userService.createUser(userDefault1).getId();

        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", idUser1, idUser2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/users/{id}/friends", idUser1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(put("/users/{id}/friends/{friendId}", idUser1, idUser2))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/users/{id}/friends", idUser1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", equalTo(2)))
                .andExpect(jsonPath("$[0].name", equalTo(userNotName.getName())));

        mockMvc.perform(delete("/users/{id}/friends/{friendId}", idUser2, idUser1))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/users/{id}/friends", idUser1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(put("/users/{id}/friends/{friendId}", idUser2, idUser1))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/users/{id}/friends/{friendId}", idUser3, idUser1))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/users/{id}/friends", idUser1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", equalTo(2)))
                .andExpect(jsonPath("$[0].name", equalTo(userNotName.getName())))
                .andExpect(jsonPath("$[1].id", equalTo(3)))
                .andExpect(jsonPath("$[1].name", equalTo(userDefault1.getName())));

        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", idUser2, idUser3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", equalTo(1)))
                .andExpect(jsonPath("$[0].name", equalTo(userDefault.getName())));
    }

    @Test
    @DisplayName("Пользователь должен обновить все поля")
    void shouldPutUser_thenStatus200() throws Exception {
        userService.createUser(userDefault);
        mockMvc.perform(
                        put("/users")
                                .content(objectMapper.writeValueAsString(userDefault1))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("yandex1@yandex.ru"))
                .andExpect(MockMvcResultMatchers.jsonPath("login").value("userDefault1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("UserTest1"))
                .andExpect(MockMvcResultMatchers.jsonPath("birthday").value("2000-01-02"));
    }


    @Test
    @DisplayName("Если поле [email] некорректно, валидатор должен сработать")
    void shouldBeValidationEmail() {
        userDefault.setEmail("yandexyandex.ru");
        Set<ConstraintViolation<User>> violations = validator.validate(userDefault);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage()
                        .equals("Поле [email] должно иметь формат адреса электронной почты")),
                "Email don't should empty");

        userNotName.setEmail("");
        violations = validator.validate(userNotName);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage().equals("Поле [email] не должно быть пустым")),
                "Email should have symbol @");
    }

    @Test
    @DisplayName("Если поле [login] некорректно, валидатор должен сработать")
    void shouldBeValidationLogin() {
        userDefault.setLogin("");
        Set<ConstraintViolation<User>> violations = validator.validate(userDefault);
        assertEquals(2, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage().equals("Поле [login] не должно быть пустым")),
                "Login don't should empty");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage()
                        .equals("Длина поля [login] должна находиться в диапазоне [от 3 до 20 символов]")),
                "login should have 3-20 symbol");

        userNotName.setLogin(null);
        violations = validator.validate(userNotName);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage().equals("Поле [login] не должно быть пустым")),
                "Login don't be null");
    }

    @Test
    @DisplayName("Если поле [name] не указано, name должно быть равно [login]")
    void shouldBeValidationName() {
        userDefault.setName(null);
        userService.createUser(userDefault);
        userService.createUser(userNotName);
        List<User> userList = userService.getAllUsers();
        assertEquals(userDefault.getLogin(), userList.get(0).getName(), "Name must be login");
        assertEquals(userNotName.getLogin(), userList.get(1).getName(), "Name must be login");
    }

    @Test
    @DisplayName("Если поле [birthday] некорректно, валидатор должен сработать")
    void shouldBeValidationBirthday() {
        userDefault.setBirthday(LocalDate.of(2077, 7, 7));
        Set<ConstraintViolation<User>> violations = validator.validate(userDefault);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage()
                .equals("Поле [birthday] должно содержать прошедшую дату")), "Birthday can't be later");

        userNotName.setBirthday(LocalDate.now());
        violations = validator.validate(userNotName);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage()
                .equals("Поле [birthday] должно содержать прошедшую дату")), "Birthday can't be later");
    }

    @Test
    @DisplayName("Если пользователь с таким [login] уже есть в базе")
    void shouldThrowExceptionSameLoginUser() {
        userService.createUser(userDefault);
        userNotName.setLogin(userDefault.getLogin());
        final ValidationException exceptionSameLoginUser = assertThrows(
                ValidationException.class,
                () -> userService.createUser(userNotName));
        assertEquals("[Field [login] invalid: [Пользователь с таким логином [userDefault] " +
                        "уже имеется в системе]]",
                exceptionSameLoginUser.getMessage());
    }

    @Test
    @DisplayName("Если пользователь с таким [email] уже есть в базе")
    void shouldThrowExceptionSameEmailAddAndPutUser() {
        userService.createUser(userDefault);
        userNotName.setEmail(userDefault.getEmail());
        final ValidationException exceptionSameEmailAddUser = assertThrows(
                ValidationException.class,
                () -> userService.createUser(userNotName));
        assertEquals("[Field [login] invalid: [Пользователь с таким email [yandex@yandex.ru] " +
                        "уже имеется в системе]]",
                exceptionSameEmailAddUser.getMessage());
    }

    @Test
    @DisplayName("Если добавить запрещенные к регистрации логины")
    void shouldThrowExceptionAddLoginUserAdmin() {
        userDefault.setLogin("admin");
        final ValidationException exceptionAddLoginUserAdmin = assertThrows(
                ValidationException.class,
                () -> userService.createUser(userDefault));
        assertEquals("[Field [login] invalid: [Регистрировать пользователя с таким именем [admin] запрещено]]",
                exceptionAddLoginUserAdmin.getMessage());
    }

}
