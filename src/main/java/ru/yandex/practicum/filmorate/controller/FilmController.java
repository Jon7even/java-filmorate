package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

import static ru.yandex.practicum.filmorate.constants.NameLogs.CLIENT_SEND_REQUEST;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Film getFilm(@PathVariable("id") int id,
                        HttpServletRequest request) {
        log.debug("{} [{}] на получение фильма по [ID={}]", CLIENT_SEND_REQUEST, request.getMethod(), id);
        return filmService.findFilmById(id);
    }

    @GetMapping
    public List<Film> getAllFilms(HttpServletRequest request) {
        log.debug("{} [{}] на получение списка всех фильмов", CLIENT_SEND_REQUEST, request.getMethod());
        return filmService.getAllFilms();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@Valid @RequestBody Film film,
                        HttpServletRequest request) {
        log.debug("{} [{}] на добавление фильма", CLIENT_SEND_REQUEST, request.getMethod());
        return filmService.addFilm(film);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film updateFilm(@Valid @RequestBody Film film,
                           HttpServletRequest request) {
        log.debug("{} [{}] на обновление фильма", CLIENT_SEND_REQUEST, request.getMethod());
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addLike(@PathVariable int id,
                        @PathVariable int userId,
                        HttpServletRequest request) {
        log.debug("{} [{}] пользователь с [ID={}] добавляет лайк фильму с [ID={}]",
                CLIENT_SEND_REQUEST, request.getMethod(), userId, id);
        filmService.addLikeByUserId(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLike(@PathVariable int id,
                           @PathVariable int userId,
                           HttpServletRequest request) {
        log.debug("{} [{}] пользователь [ID={}] удаляет лайк у фильма с [ID={}]",
                CLIENT_SEND_REQUEST, request.getMethod(), userId, id);
        filmService.removeLikeByUserId(id, userId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10", required = false) int count,
                                      HttpServletRequest request) {
        log.debug("{} [{}] на получение списка популярных фильмов", CLIENT_SEND_REQUEST, request.getMethod());
        if (!(count <= 0)) {
            return filmService.getPopularFilms(count);
        } else {
            throw new IncorrectParameterException("count");
        }
    }

}
