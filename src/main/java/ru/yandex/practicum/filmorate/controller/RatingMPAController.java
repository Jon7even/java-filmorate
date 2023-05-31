package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.FilmRatingMPA;
import ru.yandex.practicum.filmorate.service.RatingMPAService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static ru.yandex.practicum.filmorate.constants.NameLogs.CLIENT_SEND_REQUEST;

@RestController
@RequestMapping("/mpa")
@Slf4j
@RequiredArgsConstructor
public class RatingMPAController {
    private final RatingMPAService ratingMPAService;

    @GetMapping
    public List<FilmRatingMPA> getAllMPA(HttpServletRequest request) {
        log.debug("{} [{}] на получение списка всех рейтингов", CLIENT_SEND_REQUEST, request.getMethod());
        return ratingMPAService.getAllMPA();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public FilmRatingMPA getMPAById(@PathVariable("id") int id,
                                    HttpServletRequest request) {
        log.debug("{} [{}] на получение рейтинга по [ID={}]", CLIENT_SEND_REQUEST, request.getMethod(), id);
        if (!(id <= 0)) {
            return ratingMPAService.getMPAById(id);
        } else {
            throw new IncorrectParameterException("id");
        }
    }
}
