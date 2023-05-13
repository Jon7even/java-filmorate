package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistsException extends ApplicationException {
    public AlreadyExistsException(String resource) {
        super(getErrorMessage(resource), HttpStatus.CONFLICT);
    }

    private static String getErrorMessage(String resource) {
        return String.format("[%s] already exists", resource);
    }
}
