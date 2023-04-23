package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FilmService {
    private final Map<Integer, Film> films = new HashMap<>();

    private static Integer idGenerator = 1;

    private static Integer getIdGenerator() {
        return idGenerator++;
    }

    public static void setIdGenerator(Integer idGenerator) {
        FilmService.idGenerator = idGenerator;
    }

    public List<Film> getAllFilms() {
        log.info("В БД выполняется запрос на получение всех фильмов");
        return new ArrayList<>(films.values());
    }

    public Film addFilm(Film film) {
        film.setId(getIdGenerator());
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
            throw new ValidationException("Фильма с таким ID=" + filmId + " не существует.");
        }
    }

    public void resetFilmService() {
        setIdGenerator(0);
        films.clear();
    }
}
