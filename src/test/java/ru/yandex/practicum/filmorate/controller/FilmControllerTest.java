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
        filmDefault1 = new Film(1, "filmDefault1", "description1",
                LocalDate.of(1900, 1, 1), 100);
        filmDefault2 = new Film(2, "filmDefault2", "description1",
                LocalDate.of(2007, 7, 1), 300);
    }

    @Test
    void shouldCreateUser() {
        filmController.addFilm(filmDefault1);
        List<Film> filmList = filmController.getAllFilms();
        assertEquals(1, filmList.size(), "Quantity films must be 1");
        assertEquals(filmDefault1.getId(), filmList.get(0).getId(), "ID Film must be 1");
        assertEquals(filmDefault1.getName(), filmList.get(0).getName(), "Name should equals");
        assertEquals(filmDefault1.getDescription(), filmList.get(0).getDescription(), "Description should equals");
        assertEquals(filmDefault1.getReleaseDate(), filmList.get(0).getReleaseDate(), "ReleaseDate should equals");
        assertEquals(filmDefault1.getDuration(), filmList.get(0).getDuration(), "Duration should equals");
    }


}
