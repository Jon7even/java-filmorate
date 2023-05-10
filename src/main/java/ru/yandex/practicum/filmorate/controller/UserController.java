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
    public User getUser(@PathVariable int id) {
        log.debug("Клиент сделал Http запрос на получение пользователя по ID={}", id);
        return userService.findUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFriend(@PathVariable int id,
                          @PathVariable int friendId) {
        log.debug("Клиент с ID={} сделал Http запрос на добавление друга с ID={}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(@PathVariable int id,
                             @PathVariable int friendId) {
        log.debug("Клиент с ID={} сделал Http запрос на удаление друга с ID={}", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAllFriendsByUserId(@PathVariable int id) {
        log.debug("Клиент сделал Http запрос на получение списка друзей пользователя ID={}", id);
        return userService.getAllFriendsByUserId(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAllCommonFriendsByUserId(@PathVariable int id,
                                                  @PathVariable int otherId) {
        log.debug("Клиент с ID={} сделал Http запрос на получение списка общих друзей пользователя ID={}",
                id, otherId);
        return userService.getAllCommonFriendsByUserId(id, otherId);
    }

}
