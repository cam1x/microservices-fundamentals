package com.chachotkin.resource.service.config;

import com.chachotkin.resource.service.exception.BadRequestException;
import com.chachotkin.resource.service.exception.ResourceNotFoundException;
import com.chachotkin.resource.service.exception.ServiceException;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> switch (response.status()) {
            case 400 -> new BadRequestException(response.reason());
            case 404 -> new ResourceNotFoundException(response.reason());
            default -> new ServiceException(response.reason());
        };
    }
}
