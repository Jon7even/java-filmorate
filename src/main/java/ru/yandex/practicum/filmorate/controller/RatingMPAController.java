package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.FilmRatingMPA;
import ru.yandex.practicum.filmorate.service.RatingMPAService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static ru.yandex.practicum.filmorate.constants.NameLogs.CLIENT_SEND_REQUEST;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class RatingMPAController {
    private RatingMPAService ratingMPAService;

    @Autowired
    public RatingMPAController(RatingMPAService ratingMPAService) {
        this.ratingMPAService = ratingMPAService;
    }

    @GetMapping
    public List<FilmRatingMPA> getAllMPA(HttpServletRequest request) {
        log.debug("{} [{}] на получение списка всех рейтингов", CLIENT_SEND_REQUEST, request.getMethod());
        return ratingMPAService.getAllMPA();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public FilmRatingMPA getMPAById(@PathVariable("id") Integer id,
                                    HttpServletRequest request) {
        log.debug("{} [{}] на получение рейтинга по [ID={}]", CLIENT_SEND_REQUEST, request.getMethod(), id);
        if (!(id <= 0)) {
            return ratingMPAService.getMPAById(id);
        } else {
            throw new IncorrectParameterException("id");
        }
    }
}
