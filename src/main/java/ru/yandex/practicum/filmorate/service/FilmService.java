package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {

    List<Film> getAllFilms();

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film findFilmById(int idFilm);

    void addLikeByUserId(int idFilm, int idUser);

    void removeLikeByUserId(int idFilm, int idUser);

    List<Film> getPopularFilms(int count);
}
