package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;

public class NotCreatedException extends ApplicationException {
    public NotCreatedException(String resource) {
        super(getErrorMessage(resource), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static String getErrorMessage(String resource) {
        return String.format("[%s] not created", resource);
    }
}
