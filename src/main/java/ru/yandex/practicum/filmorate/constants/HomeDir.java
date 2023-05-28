package ru.yandex.practicum.filmorate.constants;

public class HomeDir {
    private HomeDir() {
    }

    public static final String HOME = System.getProperty("user.dir") + "/src/main/resources/";
    public static final String HOME_TEST = System.getProperty("user.dir") + "/src/test/resources/";
}