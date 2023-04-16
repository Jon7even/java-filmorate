/*
 * Copyright (c) 2023 HomeWork. Yandex Practicum. All rights reserved.
 *
 * DangerZone!!!
 * При изменении настройки SET_MIN_DATE, необходимо изменить сообщение
 * о событии ReleaseDateFilms. Сделать это можно в файле с названием:
 * ValidationMessages.properties. Ключ - ReleaseDateFilms.invalid
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
