package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.constans.Settings.BAN_LIST_ADD_LOGIN;
import static ru.yandex.practicum.filmorate.constans.Settings.BAN_LIST_FIND_LOGIN;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.debug("Сделан запрос на получение списка всех пользователей");
        return userService.getAllUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user) {
        log.debug("Сделан запрос на добавление нового пользователя");
        return userService.createUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.debug("Сделан запрос на обновление пользователя с ID={}", user.getId());
        return userService.updateUser(user);
    }
}
