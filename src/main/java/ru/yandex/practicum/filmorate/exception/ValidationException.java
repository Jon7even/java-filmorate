package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidationException extends ApplicationException {
    public ValidationException(Collection<Map<String, String>> fieldsError) {
        super(getErrorMessage(fieldsError), HttpStatus.BAD_REQUEST);
    }

    private static String getErrorMessage(Collection<Map<String, String>> fieldsError) {
        return fieldsError.stream().map(p -> String.format("Field %s invalid: %s", p.keySet(), p.values()))
                .collect(Collectors.joining(",", "[", "]"));
    }
}