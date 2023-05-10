package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotCreatedException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAllFilms() {
        log.debug("Сервис выполняет запрос в БД на получение всех фильмов");
        List<Film> listFilm = filmStorage.getAllFilms();
        if (listFilm.isEmpty()) {
            log.debug("Из БД вернулся пустой список фильмов");
        } else {
            log.debug("Из БД успешно получен список из count={} фильмов", listFilm.size());
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
        Film getUpdateFilm = filmStorage.updateFilm(film);

        if (getUpdateFilm != null) {
            log.debug("В БД успешно обновлен фильм {}", getUpdateFilm.getName());
        } else {
            log.error("Ошибка БД! Film is null.");
            filmNotFoundById(0);
        }
        return getUpdateFilm;
    }

    public Film findFilmById(int id) {
        filmNotFoundById(id);
        log.debug("Сервис выполняет запрос в БД на получение фильма ID={}", id);
        Film getFindFilm = filmStorage.findFilmById(id);
        if (getFindFilm != null) {
            log.debug("Из БД успешно получен фильм с ID={}", id);
        } else {
            log.error("Ошибка БД! Film is null.");
            filmNotFoundById(0);
        }
        return getFindFilm;
    }

    public void addLikeByUserId(int idFilm, int userId) {
        filmNotFoundById(idFilm);
        userNotFoundById(userId);
        userStorage.findUserById(userId);
        log.debug("Сервис выполняет запрос в БД на добавление лайка пользователя ID={} фильму ID={}", userId, idFilm);
        Film getFilm = filmStorage.addLikeByUserId(idFilm, userId);

        if (getFilm.getLikes().contains(userId)) {
            log.debug("В БД успешно обновлены данные фильма ID={} пользователь ID={} поставил лайк", userId, idFilm);
        } else {
            log.error("Ошибка БД! Film is null.");
            filmNotFoundById(0);
        }
    }

    public void removeLikeByUserId(int idFilm, int userId) {
        filmNotFoundById(idFilm);
        userNotFoundById(userId);
        userStorage.findUserById(userId);
        log.debug("Сервис выполняет запрос в БД на удаление лайка пользователя ID={} у фильма ID={}", userId, idFilm);
        Film getFilm = filmStorage.removeLikeByUserId(idFilm, userId);

        if (getFilm.getLikes().contains(userId)) {
            log.error("Ошибка БД! Film is null.");
            filmNotFoundById(0);
        } else {
            log.debug("В БД успешно обновлены данные фильма ID={} пользователь ID={} удалил лайк", userId, idFilm);
        }
    }

    public List<Film> getPopularFilms(int count) {
        log.debug("Сервис выполняет запрос в БД на получение count={} популярных фильмов", count);
        List<Film> listPopularFilms = filmStorage.getPopularFilms(count);
        if (listPopularFilms.isEmpty()) {
            log.debug("Из БД вернулся пустой список популярных фильмов");
        } else {
            log.debug("Из БД успешно получен список из count={} популярных фильмов. Изначальный запрос был count={}",
                    listPopularFilms.size(), count);
        }
        return listPopularFilms;
    }

    private void filmNotFoundById(int id) {
        if (id <= 0) {
            throw new NotFoundException(String.format("Film with ID=%d", id));
        }
    }

    private void userNotFoundById(int id) {
        if (id <= 0) {
            throw new NotFoundException(String.format("User with ID=%d", id));
        }
    }

}
