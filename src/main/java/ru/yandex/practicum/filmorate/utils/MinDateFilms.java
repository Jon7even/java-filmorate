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
 */

package ru.yandex.practicum.filmorate.utils;

import java.time.LocalDate;
import java.time.Month;

public class MinDateFilms {
    private MinDateFilms() {
    }

    public static LocalDate SET_MIN_DATE = LocalDate.of(1895, Month.DECEMBER, 28);
}
