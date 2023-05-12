package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private int id;

    @NotBlank(message = "Поле [email] не должно быть пустым")
    @Email(message = "Поле [email] должно иметь формат адреса электронной почты")
    private String email;

    @NotBlank(message = "Поле [login] не должно быть пустым")
    @Size(min = 3, max = 20, message = "Длина поля [login] должна находиться в диапазоне [от 3 до 20 символов]")
    private String login;

    private String name;

    @NotNull
    @Past(message = "Поле [birthday] должно содержать прошедшую дату")
    private LocalDate birthday;

    private Set<Integer> friends;

    public User(int id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.friends = new HashSet<>();
    }

    public void addFriend(int id) {
        friends.add(id);
    }

    public void removeFriend(int id) {
        friends.remove(id);
    }
}
