package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static javax.validation.Validation.buildDefaultValidatorFactory;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.yandex.practicum.filmorate.utils.MinDateFilms.SET_MIN_DATE;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
    private FilmStorage filmStorage;

    private Validator validator;
    private Film filmDefault1;
    private Film filmDefault2;
    private User userDefault;
    private User userDefault1;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = buildDefaultValidatorFactory();
        validator = factory.getValidator();
        initFilmsAndUser();
    }

    void initFilmsAndUser() {
        filmDefault1 = new Film(0, "filmDefault1", "description1",
                LocalDate.of(1901, 1, 1), 100, 1);
        filmDefault2 = new Film(1, "filmDefault2", "description2",
                LocalDate.of(2007, 7, 1), 300, 4);
        userDefault = new User(0, "yandex@yandex.ru", "userDefault", "UserTest",
                LocalDate.of(2000, 1, 1));
        userDefault1 = new User(1, "yandex1@yandex.ru", "userDefault1", "UserTest1",
                LocalDate.of(2000, 1, 2));
    }

    @Test
    @DisplayName("Фильм должен создаться с релевантными полями")
    void shouldCreateFilm_thenStatus201() throws Exception {
        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(filmDefault1))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("filmDefault1"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("description1"))
                .andExpect(MockMvcResultMatchers.jsonPath("releaseDate").value("1901-01-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("duration").value("100"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mpa.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mpa.name").value("G"));
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
        int idFilm = filmService.addFilm(filmDefault1).getId();
        int idFilm1 = filmService.addFilm(filmDefault2).getId();
        int idUser1 = userService.createUser(userDefault).getId();
        int idUser2 = userService.createUser(userDefault1).getId();

        mockMvc.perform(put("/films/{id}/like/{userId}", idFilm1, idUser1))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/films/popular", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(idFilm1));

        mockMvc.perform(put("/films/{id}/like/{userId}", idFilm, idUser2))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/films/{id}/like/{userId}", idFilm, idUser1))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/films/popular", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(idFilm));

        mockMvc.perform(delete("/films/{id}/like/{userId}", idFilm, idUser1))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/films/{id}/like/{userId}", idFilm, idUser2))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/films/popular", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(idFilm1));
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
