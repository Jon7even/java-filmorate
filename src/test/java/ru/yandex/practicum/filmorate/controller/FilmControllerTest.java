package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilmControllerTest {
    private FilmController filmController;
    private Film filmDefault1;
    private Film filmDefault2;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        initFilms();
    }

    void initFilms() {
        filmDefault1 = new Film(0, "filmDefault1", "description1",
                LocalDate.of(1900, 1, 1), 100);
        filmDefault2 = new Film(1, "filmDefault2", "description1",
                LocalDate.of(2007, 7, 1), 300);
    }

    @Test
    void shouldCreateFilm() {
        filmController.addFilm(filmDefault1);
        List<Film> filmList = filmController.getAllFilms();
        assertEquals(1, filmList.size(), "Quantity films must be 1");
        assertEquals(filmDefault1.getId(), filmList.get(0).getId(), "ID Film must be 1");
        assertEquals(filmDefault1.getName(), filmList.get(0).getName(), "Name should equals");
        assertEquals(filmDefault1.getDescription(), filmList.get(0).getDescription(), "Description should equals");
        assertEquals(filmDefault1.getReleaseDate(), filmList.get(0).getReleaseDate(), "ReleaseDate should equals");
        assertEquals(filmDefault1.getDuration(), filmList.get(0).getDuration(), "Duration should equals");
    }

    @Test
    void shouldPutFilm() {
        filmController.addFilm(filmDefault1);
        filmController.updateFilm(filmDefault2);
        List<Film> filmList = filmController.getAllFilms();
        assertEquals(1, filmList.size(), "Quantity films must be 1");
        assertEquals(filmDefault2.getId(), filmList.get(0).getId(), "ID Film must be 1");
        assertEquals(filmDefault2.getName(), filmList.get(0).getName(), "Name should equals");
        assertEquals(filmDefault2.getDescription(), filmList.get(0).getDescription(), "Description should equals");
        assertEquals(filmDefault2.getReleaseDate(), filmList.get(0).getReleaseDate(), "ReleaseDate should equals");
        assertEquals(filmDefault2.getDuration(), filmList.get(0).getDuration(), "Duration should equals");
    }

    @Test
    void donTShouldBeNameIsEmpty() {
        filmDefault1.setName("");
        filmController.addFilm(filmDefault1);
        List<Film> filmList = filmController.getAllFilms();
        assertEquals(0, filmList.size(), "Name cannot be empty");
    }

    @Test
    void donTShouldBeDescriptionLength200() {
        filmDefault1.setDescription("testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest" +
                "testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest" +
                "testtesttesttesttestt");
        filmController.addFilm(filmDefault1);
        List<Film> filmList = filmController.getAllFilms();
        assertEquals(0, filmList.size(), "Maximum length of the description is 200 characters");
    }

    @Test
    void donTShouldBeDataReleaseBefore() {
        filmDefault1.setReleaseDate(LocalDate.of(1007, 7, 1));
        filmController.addFilm(filmDefault1);
        List<Film> filmList = filmController.getAllFilms();
        assertEquals(0, filmList.size(), "Release date — no earlier than December 28, 1895");
    }

    @Test
    void donTShouldBeDurationMinus() {
        filmDefault1.setDuration(-1);
        filmController.addFilm(filmDefault1);
        List<Film> filmList = filmController.getAllFilms();
        assertEquals(0, filmList.size(), "Duration of the film should be positive");
    }
}
