package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmLikes;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {
    List<Film> getAllFilms();

    Optional<Film> findFilmById(Integer id);

    Optional<Film> addFilm(Film film);

    Optional<Film> updateFilm(Film film, Set<FilmGenre> genres);

    FilmLikes addLikeByUserId(FilmLikes film, Integer userId);

    FilmLikes removeLikeByUserId(FilmLikes film, Integer userId);

    List<Film> getPopularFilms(Integer count);
}
