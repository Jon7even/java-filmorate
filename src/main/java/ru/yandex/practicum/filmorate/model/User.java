package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class User {
    private int id;

    @NotNull
    @Email
    private String email;

    @NotBlank
    @Size(min = 3, max = 20)
    private String login;

    private String name;

    @NotNull
    @Past
    private LocalDate birthday;
}
