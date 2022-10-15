package com.chachotkin.resource.processor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NotFoundException extends ResponseStatusException {

    public NotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, String.format("Resource with id [%s] doesn't exist!", id));
    }

    public NotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }

    public NotFoundException(String reason, Throwable cause) {
        super(HttpStatus.NOT_FOUND, reason, cause);
    }
}
