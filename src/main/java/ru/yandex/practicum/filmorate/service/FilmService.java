package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Slf4j
@Service
public class FilmService {
    FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public List<Film> getAllFilms() {
        log.debug("Сервис выполняет запрос в БД на получение всех фильмов");
        List<Film> listFilm = filmStorage.getAllFilms();
        if (listFilm.isEmpty()) {
            log.debug("Из БД вернулся пустой список фильмов");
        } else {
            log.debug("Из БД успешно получен список фильмов");
        }
        return listFilm;
    }

    public Film addFilm(Film film) {
        log.debug("Сервис выполняет запрос в БД на добавление фильма");
        Film requestFilm = filmStorage.addFilm(film);
        if (requestFilm != null) {
            log.debug("В БД успешно добавлен новый фильм {}", requestFilm.getName());
        } else {
            log.error("БД вернула null. По неизвестной причине не получилось добавить новый фильм");
        }
        return requestFilm;
    }

    public Film updateFilm(Film film) {
        log.debug("Сервис выполняет запрос в БД на обновление фильма с id={}", film.getId());
        Film updateFilm = filmStorage.addFilm(film);
        if (updateFilm != null) {
            log.debug("В БД успешно обновлен фильм {}", updateFilm.getName());
        } else {
            log.error("БД вернула null. По неизвестной причине не получилось обновить фильм");
        }
        return updateFilm;
    }

/*    Создайте FilmService, который будет отвечать за операции с фильмами, — добавление и удаление лайка,
    вывод 10 наиболее популярных фильмов по количеству лайков. Пусть пока каждый пользователь может
    поставить лайк фильму только один раз.*/
}
