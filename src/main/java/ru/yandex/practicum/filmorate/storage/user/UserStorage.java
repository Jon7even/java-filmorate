package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Optional<User> createUser(User user);

    Optional<User> findUserById(Integer id);

    List<User> getAllUsers();

/*

    User updateUser(User user);

    User addFriend(int idUser, int idFriend);

    User removeFriend(int idUser, int idFriend);

    List<User> getAllFriendsByUserId(int idUser);

    List<User> getAllCommonFriendsByUserId(int idUser, int idFriend);*/
}
