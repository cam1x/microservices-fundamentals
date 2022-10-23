package com.chachotkin.resource.processor.config;

import com.chachotkin.resource.processor.exception.BadRequestException;
import com.chachotkin.resource.processor.exception.NotFoundException;
import com.chachotkin.resource.processor.exception.ServiceException;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> switch (response.status()) {
            case 400 -> new BadRequestException(response.reason());
            case 404 -> new NotFoundException(response.reason());
            default -> new ServiceException(response.reason());
        };
    }

    @Bean
    public Retryer retryer(@Value("${feign.retry.period}") long period,
                           @Value("${feign.retry.max-period}") long maxPeriod,
                           @Value("${feign.retry.max-attempts}") int maxAttempts) {
        return new Retryer.Default(period, maxPeriod, maxAttempts);
    }
}
