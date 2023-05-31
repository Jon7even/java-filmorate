package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User findUserById(int idUser);

    User updateUser(User user);

    User createUser(User user);

    void addFriend(int idUser, int idFriend);

    void removeFriend(int idUser, int idFriend);

    List<User> getAllFriendsByUserId(int idUser);

    List<User> getAllCommonFriendsByUserId(int idUser, int idFriend);
}
