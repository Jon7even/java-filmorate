/*
 * Copyright (c) 2023 HomeWork. Yandex Practicum. All rights reserved.
 *
 * DangerZone!!!
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

package ru.yandex.practicum.filmorate.utils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.lines;
import static java.nio.file.Paths.get;
import static ru.yandex.practicum.filmorate.constants.HomeDir.HOME;

public class BanListUserName {
    private BanListUserName() {
    }

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
