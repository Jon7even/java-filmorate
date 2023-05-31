package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserRelation;
import ru.yandex.practicum.filmorate.model.UserEnumRelationStatus;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    List<User> getAllUsers();

    Optional<User> findUserById(int id);

    Optional<User> updateUser(User user);

    Optional<User> createUser(User user);

    UserEnumRelationStatus addFriend(int idUser, int idFriend);

    UserEnumRelationStatus removeFriend(int idUser, int idFriend);

    UserRelation getAllFriendsByUserId(UserRelation user);

    List<User> getAllCommonFriendsByUserId(UserRelation userRelation, UserRelation friendRelation);
}
