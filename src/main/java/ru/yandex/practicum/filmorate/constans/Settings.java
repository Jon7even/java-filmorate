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

import java.time.LocalDate;
import java.time.Month;

public class Settings {
    public static LocalDate SET_MIN_DATE = LocalDate.of(1895, Month.DECEMBER, 28);
}
