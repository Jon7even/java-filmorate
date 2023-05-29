package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.List;

public interface GenreService {
    List<FilmGenre> getAllGenre();

    FilmGenre getGenresById(int idGenre);
}
