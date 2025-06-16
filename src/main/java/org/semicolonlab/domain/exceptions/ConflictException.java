package org.semicolonlab.domain.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ConflictException extends SemicolonLabException {


    public ConflictException(String message, HttpStatus status, String errorCode, String title, Map<String, Object> context) {
        super(message, status, errorCode, title,  context);
    }

        public ConflictException(String message, Throwable cause, HttpStatus status, String errorCode, String title, Map<String, Object> context) {
        super(message, cause, status, errorCode, title, context);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConflictException(String message) {
        super(message);
    }
}
