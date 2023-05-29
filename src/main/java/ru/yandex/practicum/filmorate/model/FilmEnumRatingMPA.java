package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonValue;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;

import java.util.HashMap;
import java.util.Map;

public enum FilmEnumRatingMPA {
    G("G", 1),
    PG("PG", 2),
    PG_13("PG-13", 3),
    R("R", 4),
    NC_17("NC-17", 5);

    private final String name;
    private final int number;
    private static final Map<Integer, FilmEnumRatingMPA> ID_MPA = new HashMap<>();
    private static final Map<String, FilmEnumRatingMPA> NAME_MPA = new HashMap<>();

    FilmEnumRatingMPA(String name, int number) {
        this.name = name;
        this.number = number;
    }

    static {
        for (FilmEnumRatingMPA f : values()) {
            ID_MPA.put(f.number, f);
            NAME_MPA.put(f.toString(), f);
        }
    }

    public static FilmEnumRatingMPA valueNumber(int number) {
        try {
            return ID_MPA.get(number);
        } catch (IllegalArgumentException e) {
            throw new IncorrectParameterException("RatingMPA id");
        }
    }

    public static FilmEnumRatingMPA valueName(String name) {
        try {
            return NAME_MPA.get(name);
        } catch (Exception e) {
            throw new IncorrectParameterException("RatingMPA name");
        }
    }

    public static Map<Integer, FilmEnumRatingMPA> getIntMPA() {
        return ID_MPA;
    }

    public Integer toInt() {
        return number;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
