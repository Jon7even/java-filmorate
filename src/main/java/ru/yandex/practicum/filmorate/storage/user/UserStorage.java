package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserRelation;
import ru.yandex.practicum.filmorate.model.UserRelationStatus;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    List<User> getAllUsers();

    Optional<User> findUserById(Integer id);

    Optional<User> updateUser(User user);

    Optional<User> createUser(User user);

    UserRelationStatus addFriend(Integer idUser, Integer idFriend);

    UserRelationStatus removeFriend(Integer idUser, Integer idFriend);

    UserRelation getAllFriendsByUserId(UserRelation user);

    List<User> getAllCommonFriendsByUserId(UserRelation userRelation, UserRelation friendRelation);
}
