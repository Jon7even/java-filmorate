package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRatingMPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static javax.validation.Validation.buildDefaultValidatorFactory;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.yandex.practicum.filmorate.utils.MinDateFilms.SET_MIN_DATE;

@SpringBootTest
@AutoConfigureMockMvc
public class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilmService filmService;

    @Autowired
    private UserService userService;

    @Autowired
    private InMemoryFilmStorage filmStorage;

    private Validator validator;
    private Film filmDefault1;
    private Film filmDefault2;
    private User userDefault;

    @BeforeEach
    void setUp() {
        filmStorage.clearRepository();
        ValidatorFactory factory = buildDefaultValidatorFactory();
        validator = factory.getValidator();
        initFilmsAndUser();
    }

    void initFilmsAndUser() {
        filmDefault1 = new Film(0, "filmDefault1", "description1",
                LocalDate.of(1900, 1, 1), 100, "default", "");
        filmDefault2 = new Film(1, "filmDefault2", "description2",
                LocalDate.of(2007, 7, 1), 300, "default", "");
        userDefault = new User(0, "yandex@yandex.ru", "userDefault", "UserTest",
                LocalDate.of(2000, 1, 1));
    }


    @Test
    @DisplayName("Фильм должен создаться с релевантными полями")
    void shouldCreateFilm_thenStatus201() throws Exception {
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(filmDefault1))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("filmDefault1"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("description1"))
                .andExpect(MockMvcResultMatchers.jsonPath("releaseDate").value("1900-01-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("duration").value("100"));
    }

    @Test
    @DisplayName("Фильм должен обновить все поля")
    void shouldPutFilm_thenStatus200() throws Exception {
        filmService.addFilm(filmDefault1);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(filmDefault2))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("filmDefault2"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("description2"))
                .andExpect(MockMvcResultMatchers.jsonPath("releaseDate").value("2007-07-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("duration").value("300"));
    }

    @Test
    @DisplayName("Поиск фильма по [ID]")
    void shouldGetFilm_thenById() throws Exception {
        long idFilm = filmService.addFilm(filmDefault1).getId();
        mockMvc.perform(get("/films/{id}", idFilm))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(idFilm))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("filmDefault1"));
        mockMvc.perform(get("/films/{id}", -1))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/films/{id}", idFilm + 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Endpoint [like]")
    void shouldAddLike_AndRemoveLike() throws Exception {
        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        int idFilm1 = filmService.addFilm(filmDefault1).getId();
        int idUser1 = userService.createUser(userDefault).getId();

        mockMvc.perform(put("/films/{id}/like/{userId}", idFilm1, idUser1))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/films/{id}", idFilm1))
                .andExpect(jsonPath("$.likes", hasSize(1)))
                .andExpect(jsonPath("$.likes[0]", equalTo(idUser1)))
                .andExpect(jsonPath("$.countLikes", equalTo(idUser1)));

        mockMvc.perform(get("/films/popular", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete("/films/{id}/like/{userId}", idFilm1, idUser1))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/films/{id}", idFilm1))
                .andExpect(jsonPath("$.likes", hasSize(0)))
                .andExpect(jsonPath("$.likes", empty()))
                .andExpect(jsonPath("$.countLikes", equalTo(0)));
    }

    @Test
    @DisplayName("Если поле [name] некорректно, валидатор должен сработать")
    void shouldNotCreateFilmWithNameIsEmpty() {
        filmDefault1.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(filmDefault1);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage().equals("Поле [name] не должно быть пустым")),
                "[name] not be empty");

        filmDefault2.setName(null);
        violations = validator.validate(filmDefault2);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage().equals("Поле [name] не должно быть пустым")),
                "[name] not be empty");
    }


    @Test
    @DisplayName("Если поле [description] некорректно, валидатор должен сработать")
    void shouldNotCreateFilmWithDescriptionLength200() {
        filmDefault1.setDescription("testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest" +
                "testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest" +
                "testtesttesttesttestt");
        Set<ConstraintViolation<Film>> violations = validator.validate(filmDefault1);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage()
                        .equals("Длина поля [description] должен быть в диапазоне [от 0 до 200 символов]")),
                "Maximum length of the [description] is 200 characters");
    }

    @Test
    @DisplayName("Если поле [genre] некорректно, валидатор должен сработать")
    void shouldNotCreateFilmWithGenreLength20() {
        filmDefault1.setGenre("test1test2test3test4t");
        Set<ConstraintViolation<Film>> violations = validator.validate(filmDefault1);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage()
                        .equals("Длина поля [genre] должна быть в диапазоне [от 0 до 20 символов]")),
                "Maximum length of the [genre] is 20 characters");
    }

    @Test
    @DisplayName("Если поле [rating] некорректно, валидатор должен сработать")
    void shouldNotCreateFilmWithRatingLength10() {
        filmDefault1.setRating("test1test2t");
        Set<ConstraintViolation<Film>> violations = validator.validate(filmDefault1);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream().anyMatch(t -> t.getMessage()
                        .equals("Длина поля [rating] должна быть в диапазоне [от 0 до 10 символов]")),
                "Maximum length of the [rating] is 10 characters");
    }

    @Test
    @DisplayName("Если поле [rating] не указано, должен быть выставлен максимальный рейтинг [NC-17]")
    void shouldBeRatingSetDefaultWhenFieldEmpty() {
        filmService.addFilm(filmDefault1);
        Film getFilm = filmService.findFilmById(1);
        assertEquals(FilmRatingMPA.NC_17.toString(), getFilm.getRating(), "Rating Film don't equals");
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
    @DisplayName("Если поле [duration] некорректно, валидатор должен сработать")
    void shouldNotCreateFilmWithDurationMinus() {
        filmDefault1.setDuration(-1);
        Set<ConstraintViolation<Film>> violations = validator.validate(filmDefault1);
        assertEquals(1, violations.size(), "Errors than necessary");
        assertTrue(violations.stream()
                        .anyMatch(t -> t.getMessage().equals("Поле [duration] должно быть положительным")),
                "[duration] of the film should be positive");
    }
}
