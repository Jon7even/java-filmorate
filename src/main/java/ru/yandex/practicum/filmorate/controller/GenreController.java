package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.service.GenreService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static ru.yandex.practicum.filmorate.constants.NameLogs.CLIENT_SEND_REQUEST;

@RestController
@RequestMapping("/genres")
@Slf4j
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public List<FilmGenre> getAllGenre(HttpServletRequest request) {
        log.debug("{} [{}] на получение списка всех жанров", CLIENT_SEND_REQUEST, request.getMethod());
        return genreService.getAllGenre();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public FilmGenre getGenresById(@PathVariable("id") int id,
                                   HttpServletRequest request) {
        log.debug("{} [{}] на получение жанра по [ID={}]", CLIENT_SEND_REQUEST, request.getMethod(), id);
        if (!(id <= 0)) {
            return genreService.getGenresById(id);
        } else {
            throw new IncorrectParameterException("id");
        }
    }
}
