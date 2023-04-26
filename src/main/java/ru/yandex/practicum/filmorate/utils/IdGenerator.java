package ru.yandex.practicum.filmorate.utils;

import org.springframework.stereotype.Service;

@Service
public class IdGenerator {
    private Integer idGenerator = 1;

    public Integer getIdGenerator() {
        return idGenerator++;
    }

    public void resetIdGenerator() {
        this.idGenerator = 1;
    }
}
