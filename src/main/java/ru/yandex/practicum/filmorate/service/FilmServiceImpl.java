package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotCreatedException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRatingMPA;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.constans.NameLogs.SERVICE_FROM_DB;
import static ru.yandex.practicum.filmorate.constans.NameLogs.SERVICE_IN_DB;
import static ru.yandex.practicum.filmorate.model.FilmRatingMPA.NC_17;

@Slf4j
@Service
public class FilmServiceImpl implements FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmServiceImpl(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAllFilms() {
        log.info("{} на получение списка всех фильмов", SERVICE_IN_DB);
        List<Film> listFilm = filmStorage.getAllFilms();
        if (listFilm.isEmpty()) {
            log.info("{} пустой список фильмов", SERVICE_FROM_DB);
        } else {
            log.info("{} успешно вернулся список из [count={}] фильмов", SERVICE_FROM_DB, listFilm.size());
        }
        return listFilm;
    }

    public Film addFilm(Film film) {
        if (isValidatedCheckGenre(film)) {
            film.setGenre("genreIsNotModerated");
        }
        film.setRating(getValidatedFilmRating(film.getRating()));

        log.info("{} на добавление нового фильма", SERVICE_IN_DB);
        Film createdFilm = filmStorage.addFilm(film);

        if (createdFilm != null) {
            log.info("{} успешно вернулся новый фильм [name={}]", SERVICE_FROM_DB, createdFilm.getName());
        } else {
            log.error("Ошибка БД! {} [Film is null]. " +
                            "По неизвестной причине не получилось добавить новый фильм [name={}]",
                    SERVICE_FROM_DB, film.getName());
            throw new NotCreatedException("New film");
        }
        return createdFilm;
    }

    public Film updateFilm(Film film) {
        int idFilm = film.getId();
        filmNotFoundByIdCheckPositive(idFilm);

        if (isValidatedCheckGenre(film)) {
            film.setGenre("genreIsNotModerated");
        }

        film.setRating(getValidatedFilmRating(film.getRating()));

        log.info("{} на обновление фильма с [ID={}]", SERVICE_IN_DB, idFilm);
        Film getUpdateFilm = filmStorage.updateFilm(film);

        if (getUpdateFilm != null) {
            log.info("{} успешно вернулся обновленный фильм [name={}]", SERVICE_FROM_DB, getUpdateFilm.getName());
        } else {
            log.error("Ошибка БД! {} [Film is null]. " +
                    "По неизвестной причине не получилось обновить фильм [name={}]", SERVICE_FROM_DB, film.getName());
            filmNotFoundByIdException(idFilm);
        }
        return getUpdateFilm;
    }

    public Film findFilmById(int idFilm) {
        filmNotFoundByIdCheckPositive(idFilm);
        log.info("{} на получение фильма [ID={}]", SERVICE_IN_DB, idFilm);
        Film getFindFilm = filmStorage.findFilmById(idFilm);
        if (getFindFilm != null) {
            log.info("{} успешно фильм с [ID={}]", SERVICE_FROM_DB, idFilm);
        } else {
            log.error("{} [Film is null] фильма с [ID={}] не существует", SERVICE_FROM_DB, idFilm);
            filmNotFoundByIdException(idFilm);
        }
        return getFindFilm;
    }

    public void addLikeByUserId(int idFilm, int idUser) {
        filmNotFoundByIdCheckPositive(idFilm);
        userNotFoundByIdCheckPositive(idUser);
        //userStorage.findUserById(idUser);
        log.info("{} на добавление лайка пользователя [ID={}] фильму [ID={}]",
                SERVICE_IN_DB, idUser, idFilm);
        Film getFilm = filmStorage.addLikeByUserId(idFilm, idUser);

        if (getFilm.getLikes().contains(idUser)) {
            log.info("{} успешно обновленный фильм: [ID={}] пользователь [ID={}] поставил лайк",
                    SERVICE_FROM_DB, idUser, idFilm);
        } else {
            log.error("{} [Film is null] фильма с [ID={}] не существует", SERVICE_FROM_DB, idFilm);
            filmNotFoundByIdException(idFilm);
        }
    }

    public void removeLikeByUserId(int idFilm, int idUser) {
        filmNotFoundByIdCheckPositive(idFilm);
        userNotFoundByIdCheckPositive(idUser);
        //userStorage.findUserById(idUser);
        log.info("{} на удаление лайка пользователя [ID={}] у фильма [ID={}]",
                SERVICE_IN_DB, idUser, idFilm);
        Film getFilm = filmStorage.removeLikeByUserId(idFilm, idUser);

        if (getFilm.getLikes().contains(idUser)) {
            log.error("{} [Film is null] фильма с [ID={}] не существует", SERVICE_FROM_DB, idFilm);
            filmNotFoundByIdException(idFilm);
        } else {
            log.info("{} успешно обновленный фильм: [ID={}] пользователь [ID={}] удалил лайк",
                    SERVICE_FROM_DB, idUser, idFilm);
        }
    }

    public List<Film> getPopularFilms(int count) {
        log.info("{} на получение [count={}] популярных фильмов", SERVICE_IN_DB, count);
        List<Film> listPopularFilms = filmStorage.getPopularFilms(count);
        if (listPopularFilms.isEmpty()) {
            log.info("{} пустой список популярных фильмов", SERVICE_FROM_DB);
        } else {
            log.info("{} успешно список из [count={}] популярных фильмов. " +
                    "Изначальный запрос был [count={}]", SERVICE_FROM_DB, listPopularFilms.size(), count);
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
        if (FilmRatingMPA.checkValidateFilmRating(rating)) {
            return rating.toUpperCase();
        } else {
            log.info("Пользователь неправильно указал возрастной рейтинг фильма. " +
                    "Поле [rating] выставляется по default. На вход было получено [{}]", rating);
            return NC_17.toString();
        }
    }

    private void filmNotFoundByIdCheckPositive(int idFilm) {
        if (idFilm <= 0) {
            throw new NotFoundException(String.format("Film with ID=%d", idFilm));
        }
    }

    private void filmNotFoundByIdException(int idFilm) {
        throw new NotFoundException(String.format("Film with ID=%d", idFilm));
    }

    private void userNotFoundByIdCheckPositive(int idUser) {
        if (idUser <= 0) {
            throw new NotFoundException(String.format("User with ID=%d", idUser));
        }
    }

}
