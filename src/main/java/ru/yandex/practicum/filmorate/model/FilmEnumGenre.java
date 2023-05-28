package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum FilmEnumGenre {
    COMEDY("Комедия", 1),
    DRAMA("Драма", 2),
    CARTOON("Мультфильм", 3),
    THRILLER("Триллер", 4),
    DOCUMENTAL("Документальный", 5),
    ACTION_MOVIE("Боевик", 6);

    private static final Map<Integer, FilmEnumGenre> ID_GENRE = new HashMap<>();
    private static final Map<String, FilmEnumGenre> NAME_GENRE = new HashMap<>();

    private final String name;
    private final int number;

    static {
        for (FilmEnumGenre f : values()) {
            ID_GENRE.put(f.number, f);
            NAME_GENRE.put(f.toString(), f);
        }
    }

    FilmEnumGenre(String name, int number) {
        this.name = name;
        this.number = number;
    }

    @Override
    public String toString() {
        return name;
    }

    public static FilmEnumGenre valueNumber(int number) {
        return ID_GENRE.get(number);
    }

    public static FilmEnumGenre valueName(String name) {
        return NAME_GENRE.get(name);
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
