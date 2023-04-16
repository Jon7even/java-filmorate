package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int idGenerator = 1;

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Сделан запрос на получение списка всех фильмов");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        film.setId(idGenerator++);
        films.put(film.getId(), film);
        log.info("В БД успешно добавлен новый фильм с ID={}", film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        int filmId = film.getId();
        if (films.containsKey(filmId)) {
            Film oldFilm = films.get(filmId);
            films.put(filmId, film);
            if (film.equals(oldFilm)) {
                log.warn("При обновлении фильма с ID={} новых данных не было " +
                        "если это сообщение повторится, на это стоит обратить внимание", filmId);
            }
            log.info("Фильм с ID={} успешно обновлен!\n Старый фильм: {},\n Новый фильм: {}",
                    filmId, oldFilm.toString(), films.get(filmId));
            return film;
        } else {
            throw new ValidationException("Фильма с таким ID=" + filmId + " не существует.");
        }
    }
}
