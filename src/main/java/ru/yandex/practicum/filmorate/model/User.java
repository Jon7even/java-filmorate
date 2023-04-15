package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.time.LocalDate;

@Data
@RequiredArgsConstructor
public class User {
    private int id;

    @NonNull
    @Email
    private String email;

    @NonNull
    @NotBlank
    private String login;

    private String name;

    @NonNull
    @Past
    private LocalDate birthday;
}
