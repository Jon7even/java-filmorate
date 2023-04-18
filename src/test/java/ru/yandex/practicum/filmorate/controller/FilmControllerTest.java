package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static javax.validation.Validation.buildDefaultValidatorFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.practicum.filmorate.constans.Settings.SET_MIN_DATE;

@SpringBootTest
public class FilmControllerTest {
    private FilmController filmController;
    private Validator validator;
    private Film filmDefault1;
    private Film filmDefault2;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        ValidatorFactory factory = buildDefaultValidatorFactory();
        validator = factory.getValidator();
        initFilms();
    }

    void initFilms() {
        filmDefault1 = new Film(0, "filmDefault1", "description1",
                LocalDate.of(1900, 1, 1), 100);
        filmDefault2 = new Film(1, "filmDefault2", "description1",
                LocalDate.of(2007, 7, 1), 300);
    }

    @Test
    @DisplayName("Фильм должен создаться с релевантными полями")
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
    @DisplayName("Фильм должен обновить все поля")
    void shouldPutFilm() {
        filmController.addFilm(filmDefault1);
        filmController.updateFilm(filmDefault2);
        List<Film> filmList = filmController.getAllFilms();
        assertEquals(1, filmList.size(), "Quantity films must be 1");
        assertEquals(filmDefault2.getId(), filmList.get(0).getId(), "ID Film must be 1");
        assertEquals(filmDefault2.getName(), filmList.get(0).getName(), "Name should equals");
        assertEquals(filmDefault2.getDescription(), filmList.get(0).getDescription(),
                "Description should equals");
        assertEquals(filmDefault2.getReleaseDate(), filmList.get(0).getReleaseDate(),
                "ReleaseDate should equals");
        assertEquals(filmDefault2.getDuration(), filmList.get(0).getDuration(), "Duration should equals");
    }


    @Test
    @DisplayName("Если поле name некорректно, валидатор должен сработать")
    void shouldNotCreateFilmWithNameIsEmpty() {
        filmDefault1.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(filmDefault1);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage().equals("Поле Name не должно быть пустым")),
                "Name not be empty");

        filmDefault2.setName(null);
        violations = validator.validate(filmDefault2);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage().equals("Поле Name не должно быть пустым")),
                "Name not be empty");
    }


    @Test
    @DisplayName("Если поле description некорректно, валидатор должен сработать")
    void shouldNotCreateFilmWithDescriptionLength200() {
        filmDefault1.setDescription("testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest" +
                "testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest" +
                "testtesttesttesttestt");
        Set<ConstraintViolation<Film>> violations = validator.validate(filmDefault1);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage()
                        .equals("размер поля Description находиться в диапазоне от 0 до 200 символов")),
                "Maximum length of the description is 200 characters");
    }

    @Test
    @DisplayName("Если дата релиза фильма раньше заданной даты, валидатор должен сработать")
    void shouldNotCreateFilmWithDataReleaseBefore() {
        filmDefault1.setReleaseDate(LocalDate.of(1007, 7, 1));
        Set<ConstraintViolation<Film>> violations = validator.validate(filmDefault1);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage()
                        .equals("Фильмы с датой релиза раньше " + SET_MIN_DATE + " нельзя публиковать")),
                "Release date — no earlier than " + SET_MIN_DATE);
    }

    @Test
    @DisplayName("Если поле Duration некорректно, валидатор должен сработать")
    void shouldNotCreateFilmWithDurationMinus() {
        filmDefault1.setDuration(-1);
        Set<ConstraintViolation<Film>> violations = validator.validate(filmDefault1);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage().equals("Поле Duration должно быть положительным")),
                "Duration of the film should be positive");
    }
}
