package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.utils.UserIdAskComparator;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class UserRelation {
    private User user;
    private Set<User> friends;
    private Comparator<User> comparator;

    public UserRelation(User user) {
        this.user = user;
        this.comparator = new UserIdAskComparator();
        this.friends = new HashSet<>();
    }

    public List<User> sortedListUser() {
        return friends.stream().sorted(comparator).collect(Collectors.toList());
    }

}

