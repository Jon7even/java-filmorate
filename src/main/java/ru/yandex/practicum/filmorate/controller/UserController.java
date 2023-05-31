package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

import static ru.yandex.practicum.filmorate.constants.NameLogs.CLIENT_SEND_REQUEST;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAllUsers(HttpServletRequest request) {
        log.debug("{} [{}] на получение списка всех пользователей", CLIENT_SEND_REQUEST, request.getMethod());
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public User getUser(@PathVariable int id,
                        HttpServletRequest request) {
        log.debug("{} [{}] на получение пользователя по [ID={}]", CLIENT_SEND_REQUEST, request.getMethod(), id);
        return userService.findUserById(id);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@Valid @RequestBody User user,
                           HttpServletRequest request) {
        log.debug("{} [{}] на обновление пользователя с [ID={}]",
                CLIENT_SEND_REQUEST, request.getMethod(), user.getId());
        return userService.updateUser(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user,
                           HttpServletRequest request) {
        log.debug("{} [{}] на добавление нового пользователя", CLIENT_SEND_REQUEST, request.getMethod());
        return userService.createUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFriend(@PathVariable int id,
                          @PathVariable int friendId,
                          HttpServletRequest request) {
        log.debug("{} [{}] пользователь [ID={}] добавляет в друзья [ID={}]",
                CLIENT_SEND_REQUEST, request.getMethod(), id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(@PathVariable int id,
                             @PathVariable int friendId,
                             HttpServletRequest request) {
        log.debug("{} [{}] пользователь [ID={}] удаляет из друзей [ID={}]",
                CLIENT_SEND_REQUEST, request.getMethod(), id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAllFriendsByUserId(@PathVariable int id,
                                            HttpServletRequest request) {
        log.debug("{} [{}] на получение списка друзей пользователя [ID={}]",
                CLIENT_SEND_REQUEST, request.getMethod(), id);
        return userService.getAllFriendsByUserId(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAllCommonFriendsByUserId(@PathVariable int id,
                                                  @PathVariable int otherId,
                                                  HttpServletRequest request) {
        log.debug("{} [{}] на получение списка общих друзей пользователей [ID={}] и [ID={}]",
                CLIENT_SEND_REQUEST, request.getMethod(), id, otherId);
        return userService.getAllCommonFriendsByUserId(id, otherId);
    }

}
