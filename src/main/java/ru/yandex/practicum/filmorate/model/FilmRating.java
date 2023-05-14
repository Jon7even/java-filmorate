package ru.yandex.practicum.filmorate.model;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum FilmRating {
    G("G"),
    PG("PG"),
    PG_13("PG-13"),
    R("R"),
    NC_17("NC-17");

    private final String value;

    FilmRating(String value) {
        this.value = value;
    }

    public static Boolean checkValidateFilmRating(String rating) {
        return Stream.of(FilmRating.values())
                .map(FilmRating::toString)
                .collect(Collectors.toList()).stream()
                .anyMatch(rating::equalsIgnoreCase);
    }

    @Override
    public String toString() {
        return value;
    }

}
