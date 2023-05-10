package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> getAllUsers();

    User createUser(User user);

    User updateUser(User user);

    User findUserById(int id);

    User addFriend(int idUser, int idFriend);

    User removeFriend(int idUser, int idFriend);

    List<User> getAllFriendsByUserId(int idUser);

    List<User> getAllCommonFriendsByUserId(int idUser, int idFriend);
}
