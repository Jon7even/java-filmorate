package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.utils.IntegerAskComparator;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class FilmLikes {
    private Film film;
    private Set<Integer> likes;
    private Comparator<Integer> comparator;

    public FilmLikes(Film film) {
        this.film = film;
        this.comparator = new IntegerAskComparator();
        this.likes = new HashSet<>();
    }

    public List<Integer> sortedListLikes() {
        return likes.stream().sorted(comparator).collect(Collectors.toList());
    }

}

