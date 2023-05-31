package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

@Data
public class FilmRatingMPA {
    private Integer id;
    private FilmEnumRatingMPA name;

    @JsonCreator
    public FilmRatingMPA(int id) {
        this.id = id;
        this.name = FilmEnumRatingMPA.valueNumber(id);
    }

    public FilmRatingMPA(String value) {
        this.id = FilmEnumRatingMPA.valueName(value).toInt();
        this.name = FilmEnumRatingMPA.valueName(value);
    }

}
