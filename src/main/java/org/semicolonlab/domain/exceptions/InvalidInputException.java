package org.semicolonlab.domain.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidInputException extends SemicolonLabException {




    public InvalidInputException(String message, Throwable cause, HttpStatus status, String errorCode, String title, Map<String, Object> context) {
        super(message, cause, status, errorCode, title, context);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidInputException(String message) {
        super(message);
    }
}
