package ru.yandex.practicum.filmorate.model.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ReleaseDateFilmsValidator.class)
public @interface ReleaseDateFilms {
    String message() default "{ReleaseDateFilms.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
