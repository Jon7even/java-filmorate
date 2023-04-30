package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

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
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAllUsers() {
        log.debug("Клиент сделал Http запрос на получение списка всех пользователей");
        return userService.getAllUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user) {
        log.debug("Клиент сделал Http запрос на добавление нового пользователя");
        return userService.createUser(user);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@Valid @RequestBody User user) {
        log.debug("Клиент сделал Http запрос на обновление пользователя с ID={}", user.getId());
        return userService.updateUser(user);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public User getUser(@PathVariable("id") int id) {
        log.debug("Клиент сделал Http запрос на получение пользователя по ID={}", id);
        return userService.findUserById(id);
    }

/*    @PutMapping
    @ResponseStatus(HttpStatus.OK) //PUT /users/{id}/friends/{friendId} — добавление в друзья.
    public User addFriend(@RequestBody User user) {
        log.debug("Клиент сделал Http запрос на обновление пользователя с ID={}", user.getId());
        return userService.updateUser(user);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK) //DELETE /users/{id}/friends/{friendId} — удаление из друзей.
    public User updateUser(@Valid @RequestBody User user) {
        log.debug("Клиент сделал Http запрос на обновление пользователя с ID={}", user.getId());
        return userService.updateUser(user);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK) //GET /users/{id}/friends — возвращаем список пользователей, являющихся его друзьями.
    public List<User> getAllUsers() {
        log.debug("Клиент сделал Http запрос на получение списка всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK) //GET /users/{id}/friends/common/{otherId} — список друзей, общих с другим пользователем.
    public List<User> getAllUsers() {
        log.debug("Клиент сделал Http запрос на получение списка всех пользователей");
        return userService.getAllUsers();
    }*/


}
