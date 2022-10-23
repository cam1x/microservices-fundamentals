package com.chachotkin.gateway.service.controller;

import com.chachotkin.gateway.service.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequestMapping("/errors")
@RestController
public class ApiGatewayErrorController {

    @GetMapping("/invalid-resource")
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorDto> invalidResource() {
        return Mono.just(new ErrorDto("INVALID_RESOURCE", "Requested resource doesn't exist."));
    }

    @GetMapping("/resource-unavailable")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<ErrorDto> resourceUnavailable() {
        return Mono.just(new ErrorDto("RESOURCE_UNAVAILABLE", "Requested resource currently not available."));
    }
}
