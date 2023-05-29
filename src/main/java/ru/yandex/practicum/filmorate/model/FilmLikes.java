package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

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

