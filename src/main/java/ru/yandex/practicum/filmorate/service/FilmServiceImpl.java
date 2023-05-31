package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotCreatedException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmLikes;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.constants.NameLogs.SERVICE_FROM_DB;
import static ru.yandex.practicum.filmorate.constants.NameLogs.SERVICE_IN_DB;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> getAllFilms() {
        log.debug("{} на получение списка всех фильмов", SERVICE_IN_DB);
        List<Film> listFilm = filmStorage.getAllFilms();

        if (listFilm.isEmpty()) {
            log.info("{} пустой список фильмов", SERVICE_FROM_DB);
        } else {
            log.info("{} успешно вернулся список из [count={}] фильмов", SERVICE_FROM_DB, listFilm.size());
        }
        return listFilm;
    }

    public Film findFilmById(int idFilm) {
        filmNotFoundByIdCheckPositive(idFilm);
        log.debug("{} на получение фильма [ID={}]", SERVICE_IN_DB, idFilm);
        Optional<Film> getFindFilm = filmStorage.findFilmById(idFilm);

        if (getFindFilm.isPresent()) {
            log.info("{} успешно фильм [ID={}] [name={}]", SERVICE_FROM_DB, idFilm, getFindFilm.get().getName());
        } else {
            log.error("{} [Film is empty] фильма с [ID={}] не существует", SERVICE_FROM_DB, idFilm);
            filmNotFoundByIdException(idFilm);
        }
        return getFindFilm.get();
    }

    public Film addFilm(Film film) {
        log.debug("{} на добавление нового фильма", SERVICE_IN_DB);
        Optional<Film> createdFilm = filmStorage.addFilm(film);

        if (createdFilm.isPresent()) {
            log.info("{} успешно вернулся новый фильм [ID={}] [name={}]",
                    SERVICE_FROM_DB, createdFilm.get().getId(), createdFilm.get().getName());
        } else {
            log.error("{} [Film is empty]. " +
                            "По неизвестной причине не получилось добавить новый фильм [name={}]",
                    SERVICE_FROM_DB, film.getName());
            throw new NotCreatedException("New film");
        }
        return createdFilm.get();
    }

    public Film updateFilm(Film film) {
        int idFilm = film.getId();
        Film checkFoundFilm = checkExistFilm(idFilm);

        if (checkFoundFilm.equals(film)) {
            log.info("{} фильм [name={}], но новых данных для обновления нет", SERVICE_FROM_DB,
                    checkFoundFilm.getName());
            log.debug("Данные из контроллера: [Film={}],\n Данные из БД: [Film={}]", film, checkFoundFilm);
            return checkFoundFilm;
        }

        log.debug("{} на обновление фильма с [ID={}]", SERVICE_IN_DB, idFilm);
        Optional<Film> getUpdateFilm = filmStorage.updateFilm(film, checkFoundFilm.getGenres());

        if (getUpdateFilm.isEmpty()) {
            log.error("{} [Film is empty]. " +
                            "По неизвестной причине не получилось обновить данные фильма [ID={}] [name={}]",
                    SERVICE_FROM_DB, film.getId(), film.getName());
            filmNotFoundByIdException(idFilm);
        } else {
            log.info("{} успешно вернулся обновленный фильм [ID={}] [name={}]",
                    SERVICE_FROM_DB, getUpdateFilm.get().getId(), getUpdateFilm.get().getName());

        }
        return getUpdateFilm.get();
    }

    public void addLikeByUserId(int idFilm, int idUser) {
        userNotFoundByIdCheckPositive(idUser);
        FilmLikes film = new FilmLikes(checkExistFilm(idFilm));
        Optional<User> user = userStorage.findUserById(idUser);

        if (user.isEmpty()) {
            userNotFoundByIdException(idUser);
        }
        log.debug("{} на добавление лайка пользователя [ID={}] фильму [ID={}]",
                SERVICE_IN_DB, idUser, idFilm);
        FilmLikes getFilmAddLike = filmStorage.addLikeByUserId(film, idUser);

        if (getFilmAddLike.getLikes().contains(idUser)) {
            log.info("{} успешно обновленный фильм: [ID={}] пользователь [ID={}] поставил лайк",
                    SERVICE_FROM_DB, idUser, idFilm);
        } else {
            log.error("{} [List Like is empty] лайк от пользователя [ID={}] не поставлен", SERVICE_FROM_DB, idFilm);
            filmNotFoundByIdException(idFilm);
        }
    }

    public void removeLikeByUserId(int idFilm, int idUser) {
        userNotFoundByIdCheckPositive(idUser);
        FilmLikes film = new FilmLikes(checkExistFilm(idFilm));
        Optional<User> user = userStorage.findUserById(idUser);

        if (user.isEmpty()) {
            userNotFoundByIdException(idUser);
        }
        log.debug("{} на удаление лайка пользователя [ID={}] у фильма [ID={}]",
                SERVICE_IN_DB, idUser, idFilm);
        FilmLikes getFilmAddLike = filmStorage.removeLikeByUserId(film, idUser);

        if (getFilmAddLike.getLikes().contains(idUser)) {
            log.error("{} лайк от пользователя [ID={}] фильму [ID={}] не был удален",
                    SERVICE_FROM_DB, idUser, idFilm);
            filmNotFoundByIdException(idFilm);
        } else {
            log.info("{} успешно обновленный фильм: [ID={}] пользователь [ID={}] удалил лайк",
                    SERVICE_FROM_DB, idUser, idFilm);
        }
    }

    public List<Film> getPopularFilms(int count) {
        log.info("{} на получение [COUNT={}] популярных фильмов", SERVICE_IN_DB, count);
        List<Film> listPopularFilms = filmStorage.getPopularFilms(count);
        if (listPopularFilms.isEmpty()) {
            log.info("{} пустой список популярных фильмов", SERVICE_FROM_DB);
        } else {
            log.info("{} успешно список из [COUNT={}] популярных фильмов. " +
                    "Изначальный запрос был [COUNT={}]", SERVICE_FROM_DB, listPopularFilms.size(), count);
        }
        return listPopularFilms;
    }

    private Film checkExistFilm(int idFilm) {
        filmNotFoundByIdCheckPositive(idFilm);
        log.debug("{} на проверку пользователя с [ID={}]", SERVICE_IN_DB, idFilm);
        Optional<Film> checkFoundFilm = filmStorage.findFilmById(idFilm);
        if (checkFoundFilm.isEmpty()) {
            filmNotFoundByIdException(idFilm);
        }
        return checkFoundFilm.get();
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

    private void userNotFoundByIdException(int idUser) {
        throw new NotFoundException(String.format("User with ID=%d", idUser));
    }

}
