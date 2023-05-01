package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;

public class IncorrectParameterException extends ApplicationException {
    public IncorrectParameterException(String resource) {
        super(getErrorMessage(resource), HttpStatus.BAD_REQUEST);
    }

    private static String getErrorMessage(String resource) {
        return String.format("Parameter [%s] incorrect", resource);
    }
}
