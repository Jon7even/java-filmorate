package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    public FilmLikes(Film film) {
        this.film = film;
        this.likes = new HashSet<>();
    }

    public Integer getCountLikes() {
        return likes.size();
    }
}

