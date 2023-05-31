package ru.yandex.practicum.filmorate.model.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

import static ru.yandex.practicum.filmorate.utils.MinDateFilms.SET_MIN_DATE;

public class ReleaseDateFilmsValidator implements ConstraintValidator<ReleaseDateFilms, LocalDate> {
    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        return !date.isBefore(SET_MIN_DATE);
    }
}
