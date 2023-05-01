package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;

public class NotRemovedException extends ApplicationException {
    public NotRemovedException(String resource) {
        super(getErrorMessage(resource), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static String getErrorMessage(String resource) {
        return String.format("[%s] not removed", resource);
    }
}
