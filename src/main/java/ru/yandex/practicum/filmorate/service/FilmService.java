package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotCreatedException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.model.FilmRating.NC_17;

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
        log.info("Сервис выполняет запрос в БД на получение списка всех фильмов");
        List<Film> listFilm = filmStorage.getAllFilms();
        if (listFilm.isEmpty()) {
            log.info("В сервис из БД вернулся пустой список фильмов");
        } else {
            log.info("В сервис из БД успешно вернулся список из [count={}] фильмов", listFilm.size());
        }
        return listFilm;
    }

    public Film addFilm(Film film) {
        if (isValidatedCheckGenre(film)) {
            film.setGenre("genreIsNotModerated");
        }
        film.setRating(getValidatedFilmRating(film.getRating()));

        log.info("Сервис выполняет запрос в БД на добавление нового фильма");
        Film createdFilm = filmStorage.addFilm(film);

        if (createdFilm != null) {
            log.info("В сервис из БД успешно вернулся новый фильм [name={}]", createdFilm.getName());
        } else {
            log.error("Ошибка БД! В сервис из БД вернулся [Film is null]. " +
                    "По неизвестной причине не получилось добавить новый фильм [name={}]", film.getName());
            throw new NotCreatedException("New film");
        }
        return createdFilm;
    }

    public Film updateFilm(Film film) {
        filmNotFoundById(film.getId());

        if (isValidatedCheckGenre(film)) {
            film.setGenre("genreIsNotModerated");
        }

        film.setRating(getValidatedFilmRating(film.getRating()));

        log.info("Сервис выполняет запрос в БД на обновление фильма с [ID={}]", film.getId());
        Film getUpdateFilm = filmStorage.updateFilm(film);

        if (getUpdateFilm != null) {
            log.info("В сервис из БД успешно вернулся обновленный фильм [name={}]", getUpdateFilm.getName());
        } else {
            log.error("Ошибка БД! В сервис из БД вернулся [Film is null]. " +
                    "По неизвестной причине не получилось обновить фильм [name={}]", film.getName());
            filmNotFoundById(0);
        }
        return getUpdateFilm;
    }

    public Film findFilmById(int idFilm) {
        filmNotFoundById(idFilm);
        log.info("Сервис выполняет запрос в БД на получение фильма [ID={}]", idFilm);
        Film getFindFilm = filmStorage.findFilmById(idFilm);
        if (getFindFilm != null) {
            log.info("В сервис из БД успешно получен фильм с [ID={}]", idFilm);
        } else {
            log.error("В сервис из БД вернулся [Film is null] фильма с [ID={}] не существует", idFilm);
            filmNotFoundById(0);
        }
        return getFindFilm;
    }

    public void addLikeByUserId(int idFilm, int userId) {
        filmNotFoundById(idFilm);
        userNotFoundById(userId);
        userStorage.findUserById(userId);
        log.info("Сервис выполняет запрос в БД на добавление лайка пользователя [ID={}] фильму [ID={}]",
                userId, idFilm);
        Film getFilm = filmStorage.addLikeByUserId(idFilm, userId);

        if (getFilm.getLikes().contains(userId)) {
            log.info("В сервис из БД пришли обновленные данные фильма [ID={}] пользователь [ID={}] поставил лайк",
                    userId, idFilm);
        } else {
            log.error("В сервис из БД вернулся [Film is null] фильма с [ID={}] не существует", idFilm);
            filmNotFoundById(0);
        }
    }

    public void removeLikeByUserId(int idFilm, int userId) {
        filmNotFoundById(idFilm);
        userNotFoundById(userId);
        userStorage.findUserById(userId);
        log.info("Сервис выполняет запрос в БД на удаление лайка пользователя [ID={}] у фильма [ID={}]",
                userId, idFilm);
        Film getFilm = filmStorage.removeLikeByUserId(idFilm, userId);

        if (getFilm.getLikes().contains(userId)) {
            log.error("В сервис из БД вернулся [Film is null] фильма с [ID={}] не существует", idFilm);
            filmNotFoundById(0);
        } else {
            log.info("В сервис из БД успешно пришли обновленные данные фильма " +
                    "[ID={}] пользователь [ID={}] удалил лайк", userId, idFilm);
        }
    }

    public List<Film> getPopularFilms(int count) {
        log.info("Сервис выполняет запрос в БД на получение [count={}] популярных фильмов", count);
        List<Film> listPopularFilms = filmStorage.getPopularFilms(count);
        if (listPopularFilms.isEmpty()) {
            log.info("В сервис из БД вернулся пустой список популярных фильмов");
        } else {
            log.info("В сервис из БД успешно вернулся список из [count={}] популярных фильмов. " +
                    "Изначальный запрос был [count={}]", listPopularFilms.size(), count);
        }
        return listPopularFilms;
    }

    private Boolean isValidatedCheckGenre(Film film) {
        if (film.getGenre() == null || film.getGenre().isBlank()) {
            log.info("Пользователь не указал жанр фильма. Поле [genre] выставляется по default");
            return true;
        }
        return false;
    }

    private String getValidatedFilmRating(String rating) {
        if (rating == null || rating.isBlank()) {
            log.info("Пользователь не указал возрастной рейтинг фильма. " +
                    "Поле [rating] выставляется по default. На вход было получено [{}]", rating);
            return NC_17.toString();
        }
        if (FilmRating.checkValidateFilmRating(rating)) {
            return rating.toUpperCase();
        } else {
            log.info("Пользователь неправильно указал возрастной рейтинг фильма. " +
                    "Поле [rating] выставляется по default. На вход было получено [{}]", rating);
            return NC_17.toString();
        }
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
