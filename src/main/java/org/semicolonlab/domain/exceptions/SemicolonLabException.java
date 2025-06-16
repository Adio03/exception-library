package org.semicolonlab.domain.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@Setter
public class SemicolonLabException extends NestedRuntimeException {
    private HttpStatus status;
    private String errorCode;
    private String title;
    private Map<String, Object> context;

    public SemicolonLabException(String message, HttpStatus status, String errorCode, String title, Map<String, Object> context) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.title = title;
        this.context = context;
    }

    public SemicolonLabException(String message, Throwable cause, HttpStatus status, String errorCode, String title, Map<String, Object> context) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.title = title;
        this.context = context;
    }

    public SemicolonLabException(String message, Throwable cause) {
        super(message, cause);
    }

    public SemicolonLabException(String message) {
        super(message);
    }




}