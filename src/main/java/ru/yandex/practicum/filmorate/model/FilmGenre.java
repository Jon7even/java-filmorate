package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

@Data
public class FilmGenre {
    private int id;
    private FilmEnumGenre name;

    @JsonCreator
    public FilmGenre(int id) {
        this.id = id;
        this.name = FilmEnumGenre.valueNumber(id);
    }

    public FilmGenre(int id, String name) {
        this.id = id;
        this.name = FilmEnumGenre.valueName(name);
    }
}
