package ru.yandex.practicum.filmorate.utils;

public class IdGenerator {
    private Integer idGenerator = 1;

    public Integer getIdGenerator() {
        return idGenerator++;
    }

    public void resetIdGenerator() {
        this.idGenerator = 1;
    }
}
