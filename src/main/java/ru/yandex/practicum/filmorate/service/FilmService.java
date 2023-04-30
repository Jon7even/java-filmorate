package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotCreatedException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
        Film createdFilm = filmStorage.addFilm(film);
        if (createdFilm != null) {
            log.debug("В БД успешно добавлен новый фильм {}", createdFilm.getName());
        } else {
            log.error("Ошибка БД! Film is null. По неизвестной причине не получилось добавить новый фильм");
            throw new NotCreatedException("New film");
        }
        return createdFilm;
    }

    public Film updateFilm(Film film) {
        filmNotFoundById(film.getId());
        log.debug("Сервис выполняет запрос в БД на обновление фильма с id={}", film.getId());
        Film updateFilm = filmStorage.addFilm(film);
        if (updateFilm != null) {
            log.debug("В БД успешно обновлен фильм {}", updateFilm.getName());
        } else {
            filmNotFoundById(0);
        }
        return updateFilm;
    }

    public Film findFilmById(int id) {
        filmNotFoundById(id);
        log.debug("Сервис выполняет запрос в БД на получение фильма ID={}", id);
        Film getFilm = filmStorage.findFilmById(id);
        if (getFilm != null) {
            log.debug("Из БД успешно получен фильм с ID={}", id);
        } else {
            filmNotFoundById(0);
        }
        return getFilm;
    }

    private void filmNotFoundById(int id) {
        if (id <= 0) {
            throw new NotFoundException(String.format("Film with ID=%d", id));
        }
    }


/*    Создайте FilmService, который будет отвечать за операции с фильмами, — добавление и удаление лайка,
    вывод 10 наиболее популярных фильмов по количеству лайков. Пусть пока каждый пользователь может
    поставить лайк фильму только один раз.*/
}
