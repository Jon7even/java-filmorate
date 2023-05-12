package ru.yandex.practicum.filmorate.model;

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

    @Size(max = 20, message = "Длина поля [genre] должна быть в диапазоне [от 0 до 20 символов]")
    private String genre;

    @Size(max = 10, message = "Длина поля [rating] должна быть в диапазоне [от 0 до 10 символов]")
    private String rating;

    private Set<Integer> likes;

    public Film(int id, String name, String description, LocalDate releaseDate, long duration, String genre,
                String rating) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.genre = genre;
        this.rating = rating;
        this.likes = new HashSet<>();
    }

    public void addLike(int id) {
        likes.add(id);
    }

    public void removeLike(int id) {
        likes.remove(id);
    }

    public int getCountLikes() {
        return likes.size();
    }
}
