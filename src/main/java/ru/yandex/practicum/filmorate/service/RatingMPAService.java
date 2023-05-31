package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.FilmRatingMPA;

import java.util.List;

public interface RatingMPAService {
    List<FilmRatingMPA> getAllMPA();

    FilmRatingMPA getMPAById(int idMPA);
}
