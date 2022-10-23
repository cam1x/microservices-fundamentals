package com.chachotkin.resource.processor.client;

import com.chachotkin.resource.processor.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "${com.chachotkin.service.resource.name}",
        url = "${com.chachotkin.service.api-gateway.url}",
        path = "/resources",
        configuration = FeignConfig.class
)
public interface ResourceServiceClient {

    @GetMapping("/{id}")
    ByteArrayResource download(@PathVariable Long id,
                               @RequestHeader(value = HttpHeaders.RANGE, required = false) String range);

    @GetMapping("/{id}")
    ByteArrayResource download(@PathVariable Long id);
}
