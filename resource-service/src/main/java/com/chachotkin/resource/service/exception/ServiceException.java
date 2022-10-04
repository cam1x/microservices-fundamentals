package com.chachotkin.resource.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ServiceException extends ResponseStatusException {

    public ServiceException(String reason) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, reason);
    }

    public ServiceException(String reason, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, reason, cause);
    }
}
