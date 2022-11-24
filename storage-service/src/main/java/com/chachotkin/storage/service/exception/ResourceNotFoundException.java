package com.chachotkin.storage.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ResourceNotFoundException extends ResponseStatusException {

    public ResourceNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, String.format("Resource with id [%s] doesn't exist!", id));
    }

    public ResourceNotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }

    public ResourceNotFoundException(String reason, Throwable cause) {
        super(HttpStatus.NOT_FOUND, reason, cause);
    }
}
