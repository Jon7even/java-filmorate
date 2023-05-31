package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.List;
import java.util.Optional;

public interface GenreStorage {
    List<FilmGenre> getAllGenre();

    Optional<FilmGenre> getGenresById(int idGenre);
}
