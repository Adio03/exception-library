package org.semicolonlab.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UnAuthorizeException extends ResponseStatusException {
    public UnAuthorizeException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }

}
