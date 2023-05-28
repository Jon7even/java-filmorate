package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> getAllFilms();

    Optional<Film> findFilmById(Integer id);

    Optional<Film> addFilm(Film film);

    Optional<Film> updateFilm(Film film);


/*    Film addLikeByUserId(Integer idFilm, Integer userId);

    Film removeLikeByUserId(Integer idFilm, Integer userId);

    List<Film> getPopularFilms(Integer count);*/
}
