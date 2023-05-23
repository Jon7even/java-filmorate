package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotCreatedException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utils.IdGenerator;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.constans.NameLogs.DB_RUNNING;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final IdGenerator id;
    private final Map<Integer, Film> films;

    public InMemoryFilmStorage() {
        this.id = new IdGenerator();
        this.films = new HashMap<>();
    }

    public List<Film> getAllFilms() {
        log.debug("{} на получение списка всех фильмов", DB_RUNNING);
        return new ArrayList<>(films.values());
    }

    public Film addFilm(Film film) {
        log.debug("{} на добавление нового фильма", DB_RUNNING);
        int newId = id.getIdGenerator();

        if (newId <= 0) {
            log.error("Неизвестная ошибка генерации ID.");
            throw new NotCreatedException("New film");
        }

        film.setId(newId);
        films.put(film.getId(), film);
        Film createdFilm = films.get(film.getId());
        log.debug("В БД успешно добавлен новый фильм {}", createdFilm);
        return createdFilm;
    }

    public Film updateFilm(Film film) {
        log.debug("{} на обновление фильма с [ID={}]", DB_RUNNING, id);
        int filmId = film.getId();

        if (films.containsKey(filmId)) {
            Film oldFilm = films.get(filmId);

            films.put(filmId, film);
            Film updateFilm = films.get(filmId);

            if (updateFilm.equals(oldFilm)) {
                log.warn("При обновлении фильма с [ID={}] новых данных не было " +
                        "если это сообщение повторится, на это стоит обратить внимание", filmId);
            }

            log.debug("Фильм с [ID={}] успешно обновлен в БД!\n Старый фильм: {},\n Новый фильм: {}",
                    filmId, oldFilm, updateFilm);
            return updateFilm;
        } else {
            throw new NotFoundException(String.format("Film with ID=%d", filmId));
        }
    }

    public Film findFilmById(int id) {
        log.debug("{} на получение фильма с [ID={}]", DB_RUNNING, id);
        if (films.containsKey(id)) {
            return films.get(id);
        } else {
            throw new NotFoundException(String.format("Film with ID=%d", id));
        }
    }

    public Film addLikeByUserId(int idFilm, int userId) {
        log.debug("{} на добавление лайка фильму [ID={}] пользователем [ID={}]", DB_RUNNING, idFilm, userId);
        Film findFilm = findFilmById(idFilm);
        if (findFilm.getLikes().contains(userId)) {
            log.error("У фильма [ID={}] уже есть лайк [ID={}]", idFilm, userId);
            throw new AlreadyExistsException(String.format("Like by user ID=%d", userId));
        }
        findFilm.addLike(userId);
        updateFilm(findFilm);
        Film addedLikeFilm = findFilmById(idFilm);

        if (addedLikeFilm.getLikes().contains(userId)) {
            log.debug("Фильму [ID={}] успешно поставил лайк пользователь [ID={}]", idFilm, userId);
            return addedLikeFilm;
        } else {
            log.error("Ошибка БД. Фильму [ID={}] пользователь [ID={}] лайк не поставил", idFilm, userId);
            throw new NotCreatedException(String.format("New like for film ID=%d", idFilm));
        }
    }

    public Film removeLikeByUserId(int idFilm, int userId) {
        log.debug("{} на удаление лайка фильму [ID={}] пользователем [ID={}]", DB_RUNNING, idFilm, userId);
        Film findFilm = findFilmById(idFilm);

        if (findFilm.getLikes().contains(userId)) {
            findFilm.removeLike(userId);
            updateFilm(findFilm);
        } else {
            throw new NotFoundException(String.format("Like film ID=%d by user ID=%d", idFilm, userId));
        }
        Film removedLikeFilm = findFilmById(idFilm);

        if (removedLikeFilm.getLikes().contains(userId)) {
            log.error("Ошибка БД. Фильму [ID={}] пользователь [ID={}] лайк не удалил", idFilm, userId);
            throw new NotCreatedException(String.format("Like for film ID=%d by user ID=%d", idFilm, userId));
        } else {
            log.debug("Фильму [ID={}] успешно удалил лайк пользователь ID={}", idFilm, userId);
            return removedLikeFilm;
        }
    }

    public List<Film> getPopularFilms(int count) {
        log.debug("{} на получение списка [count={}] популярных фильмов", DB_RUNNING, count);
        return films.values().stream()
                .sorted(Comparator.comparingInt(Film::getCountLikes)
                        .reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void clearRepository() {
        id.resetIdGenerator();
        films.clear();
    }
}
