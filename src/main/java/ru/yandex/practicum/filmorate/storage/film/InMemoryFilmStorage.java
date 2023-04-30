package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utils.IdGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    IdGenerator id;
    private final Map<Integer, Film> films;

    public InMemoryFilmStorage() {
        this.id = new IdGenerator();
        this.films = new HashMap<>();
    }

    public List<Film> getAllFilms() {
        log.info("В БД выполняется запрос на получение списка всех фильмов");
        return new ArrayList<>(films.values());
    }

    public Film addFilm(Film film) {
        film.setId(id.getIdGenerator());
        films.put(film.getId(), film);
        Film createdFilm = films.get(film.getId());
        log.info("В БД успешно добавлен новый фильм {}", createdFilm);
        return createdFilm;
    }

    public Film updateFilm(Film film) {
        int filmId = film.getId();
        if (films.containsKey(filmId)) {
            Film oldFilm = films.get(filmId);
            films.put(filmId, film);
            Film updateFilm = films.get(filmId);
            if (updateFilm.equals(oldFilm)) {
                log.warn("При обновлении фильма с ID={} новых данных не было " +
                        "если это сообщение повторится, на это стоит обратить внимание", filmId);
            }
            log.info("Фильм с ID={} успешно обновлен в БД!\n Старый фильм: {},\n Новый фильм: {}",
                    filmId, oldFilm, updateFilm);
            return updateFilm;
        } else {
            throw new NotFoundException(String.format("Film with ID=%d", filmId));
        }
    }

    public Film findFilmById(int id) {
        log.info("В БД выполняется запрос на получение фильма с ID={}", id);
        if (films.containsKey(id)) {
            return films.get(id);
        } else {
            throw new NotFoundException(String.format("Film with ID=%d", id));
        }
    }

    public void clearRepository() {
        id.resetIdGenerator();
        films.clear();
    }
}
