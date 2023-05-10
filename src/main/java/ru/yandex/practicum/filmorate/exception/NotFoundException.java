package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApplicationException {
    public NotFoundException(String resource) {
        super(getErrorMessage(resource), HttpStatus.NOT_FOUND);
    }

    private static String getErrorMessage(String resource) {
        return String.format("[%s] not found", resource);
    }
}
