package org.semicolonlab.domain.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ResourceNotFoundException extends SemicolonLabException{



    public ResourceNotFoundException(String message, Throwable cause, HttpStatus status, String errorCode, String title, Map<String, Object> context) {
        super(message, cause, status, errorCode, title, context);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
