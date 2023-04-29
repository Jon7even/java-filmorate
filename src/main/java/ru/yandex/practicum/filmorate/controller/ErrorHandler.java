package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.yandex.practicum.filmorate.exception.ApplicationException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ApplicationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected Map<String, String> handleValidationException(ApplicationException e, WebRequest request) {
        return Map.of(
                "errorMessage", e.getErrorMessage()
        );
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected Map<String, String> handleThrowable(final Throwable e) {
        return Map.of(
                "errorMessage", "Unknown"
        );
    }
}
