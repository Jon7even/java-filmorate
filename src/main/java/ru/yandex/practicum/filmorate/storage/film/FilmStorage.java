package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    List<Film> getAllFilms();

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film findFilmById(int id);

    Film addLikeByUserId(int idFilm, int userId);

    Film removeLikeByUserId(int idFilm, int userId);

    List<Film> getPopularFilms(int count);
}
