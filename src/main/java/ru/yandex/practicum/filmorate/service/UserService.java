package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User findUserById(Integer idUser);

    User updateUser(User user);

    User createUser(User user);

    void addFriend(Integer idUser, Integer idFriend);

    void removeFriend(Integer idUser, Integer idFriend);

    List<User> getAllFriendsByUserId(Integer idUser);

    List<User> getAllCommonFriendsByUserId(Integer idUser, Integer idFriend);
}
