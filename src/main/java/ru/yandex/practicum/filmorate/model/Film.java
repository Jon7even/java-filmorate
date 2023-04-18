package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.validation.ReleaseDateFilms;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

import static ru.yandex.practicum.filmorate.constans.Settings.SET_MIN_DATE;

@Data
public class Film {
    private int id;

    @NotBlank(message = "Поле Name не должно быть пустым")
    private String name;

    @Size(max = 200, message = "размер поля Description находиться в диапазоне от 0 до 200 символов")
    private String description;

    @NotNull
    @ReleaseDateFilms()
    private LocalDate releaseDate;

    @NotNull
    @Positive(message = "Поле Duration должно быть положительным")
    private long duration;

    public Film(int id, String name, String description, LocalDate releaseDate, long duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }
}
