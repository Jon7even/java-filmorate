package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;

public class ApplicationException extends RuntimeException {
    String errorCode;
    String errorMessage;
    HttpStatus responseStatus;

    public ApplicationException(String errorMessage, HttpStatus responseStatus) {
        super();
        this.errorCode = responseStatus.toString();
        this.errorMessage = errorMessage;
        this.responseStatus = responseStatus;
    }

    public ApplicationException(String errorCode, String errorMessage, HttpStatus responseStatus) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.responseStatus = responseStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public HttpStatus getResponseStatus() {
        return responseStatus;
    }
}
