package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User createUser(User user);

    User updateUser(User user);

    User findUserById(int idUser);

    void addFriend(Integer idUser, Integer idFriend);

    void removeFriend(int idUser, int idFriend);

    List<User> getAllFriendsByUserId(int idUser);

    List<User> getAllCommonFriendsByUserId(int idUser, int idFriend);
}
