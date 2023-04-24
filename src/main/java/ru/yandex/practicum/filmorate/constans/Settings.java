/*
 * Copyright (c) 2023 HomeWork. Yandex Practicum. All rights reserved.
 *
 * DangerZone!!!
 *
 * SET_MIN_DATE
 * Общий запрет внесения фильма, раньше определенной даты.
 * При изменении настройки SET_MIN_DATE, необходимо изменить сообщение
 * о событии ReleaseDateFilms. Сделать это можно в файле с названием:
 * ValidationMessages.properties. Ключ - ReleaseDateFilms.invalid
 *
 * BAN_LIST_ADD_LOGIN
 * Список логинов, которые запрещены для регистрации и обновления.
 * Эндпоинты: createUser("/users") и updateUser("/users")
 * Редактируйте список в файле: BanListAddLogin.properties
 *
 * BAN_LIST_FIND_LOGIN
 * Список логинов, которые не будут отображаться при запросе общего
 * списка пользователей. Эндпоинт: getAllUsers("/users")
 * Редактируйте список в файле: BanListFindLogin.properties
 *
 */

package ru.yandex.practicum.filmorate.constans;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.lines;
import static java.nio.file.Paths.get;

public class Settings {
    public static LocalDate SET_MIN_DATE = LocalDate.of(1895, Month.DECEMBER, 28);
    private static final String HOME = System.getProperty("user.dir") + "/src/main/resources/";
    public static List<String> BAN_LIST_ADD_LOGIN;
    public static List<String> BAN_LIST_FIND_LOGIN;

    static {
        try {
            BAN_LIST_ADD_LOGIN = lines(get(HOME, "BanListAddLogin.properties")).collect(Collectors.toList());
            BAN_LIST_FIND_LOGIN = lines(get(HOME, "BanListFindLogin.properties")).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
