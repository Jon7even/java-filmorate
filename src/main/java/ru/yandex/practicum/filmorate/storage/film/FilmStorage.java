package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmLikes;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {
    List<Film> getAllFilms();

    Optional<Film> findFilmById(int id);

    Optional<Film> addFilm(Film film);

    Optional<Film> updateFilm(Film film, Set<FilmGenre> genres);

    FilmLikes addLikeByUserId(FilmLikes film, int userId);

    FilmLikes removeLikeByUserId(FilmLikes film, int userId);

    List<Film> getPopularFilms(int count);
}
