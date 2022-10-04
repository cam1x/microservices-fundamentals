package com.chachotkin.resource.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ResourceAlreadyExistsException extends ResponseStatusException {

    public ResourceAlreadyExistsException(String reason) {
        super(HttpStatus.CONFLICT, reason);
    }

    public ResourceAlreadyExistsException(String reason, Throwable cause) {
        super(HttpStatus.CONFLICT, reason, cause);
    }
}
