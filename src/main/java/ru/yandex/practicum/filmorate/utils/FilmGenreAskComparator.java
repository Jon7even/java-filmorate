package ru.yandex.practicum.filmorate.utils;

import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.Comparator;

public class FilmGenreAskComparator implements Comparator<FilmGenre> {
    @Override
    public int compare(FilmGenre o1, FilmGenre o2) {
        return o1.getId() - o2.getId();
    }
}
