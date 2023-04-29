package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utils.IdGenerator;

import java.util.*;

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
        log.info("В БД успешно добавлен новый фильм {}", films.get(film.getId()));
        return film;
    }

    public Film updateFilm(Film film) {
        int filmId = film.getId();
        if (films.containsKey(filmId)) {
            Film oldFilm = films.get(filmId);
            films.put(filmId, film);
            if (film.equals(oldFilm)) {
                log.warn("При обновлении фильма с ID={} новых данных не было " +
                        "если это сообщение повторится, на это стоит обратить внимание", filmId);
            }
            log.info("Фильм с ID={} успешно обновлен в БД!\n Старый фильм: {},\n Новый фильм: {}",
                    filmId, oldFilm.toString(), films.get(filmId));
            return film;
        } else {
            throw new ValidationException(Collections.singleton(Map.of("errorValidation",
                    String.format("Фильм с таким ID=%d уже имеется в системе", filmId))));
        }
    }

    public void clearRepository() {
        id.resetIdGenerator();
        films.clear();
    }
}
