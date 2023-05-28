package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
    List<Film> getAllFilms();

    Film findFilmById(Integer idFilm);

    Film addFilm(Film film);

    Film updateFilm(Film film);

/*    void addLikeByUserId(Integer idFilm, Integer idUser);

    void removeLikeByUserId(Integer idFilm, Integer idUser);

    List<Film> getPopularFilms(Integer count);*/
}
