package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.validation.ReleaseDateFilms;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private int id;

    @NotBlank(message = "Поле [name] не должно быть пустым")
    private String name;

    @Size(max = 200, message = "Длина поля [description] должен быть в диапазоне [от 0 до 200 символов]")
    private String description;

    @NotNull
    @ReleaseDateFilms()
    private LocalDate releaseDate;

    @NotNull
    @Positive(message = "Поле [duration] должно быть положительным")
    private long duration;

    private FilmRatingMPA mpa;

    private Set<FilmGenre> genres;

    @JsonCreator
    public Film(int id, String name, String description, LocalDate releaseDate, long duration, int mpaInt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = new FilmRatingMPA(mpaInt);
        this.genres = new HashSet<>();
    }

    public Film(int id, String name, String description, LocalDate releaseDate, long duration, String mpaString) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = new FilmRatingMPA(mpaString);
        this.genres = new HashSet<>();
    }

}
