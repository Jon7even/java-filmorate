package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Клиент сделал Http запрос на получение списка всех фильмов");
        return filmService.getAllFilms();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@Valid @RequestBody Film film) {
        log.debug("Клиент сделал Http запрос на добавление фильма");
        return filmService.addFilm(film);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.debug("Клиент сделал Http запрос на обновление фильма");
        return filmService.updateFilm(film);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Film getFilm(@PathVariable("id") int id) {
        log.debug("Клиент сделал Http запрос на получение фильма по [ID={}]", id);
        return filmService.findFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addLike(@PathVariable int id,
                        @PathVariable int userId) {
        log.debug("Клиент с [ID={}] сделал Http запрос на добавление лайка фильму с [ID={}]", userId, id);
        filmService.addLikeByUserId(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLike(@PathVariable int id,
                           @PathVariable int userId) {
        log.debug("Клиент с [ID={}] сделал Http запрос на удаление лайка у фильма с [ID={}]", userId, id);
        filmService.removeLikeByUserId(id, userId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10", required = false) Integer count) {
        log.debug("Клиент сделал Http запрос на получение списка популярных фильмов");
        if (!(count <= 0)) {
            return filmService.getPopularFilms(count);
        } else {
            throw new IncorrectParameterException("count");
        }
    }

}
