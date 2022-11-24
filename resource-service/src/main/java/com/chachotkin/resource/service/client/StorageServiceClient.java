package com.chachotkin.resource.service.client;

import com.chachotkin.resource.service.config.FeignConfig;
import com.chachotkin.resource.service.dto.StorageDto;
import com.chachotkin.resource.service.dto.StorageType;
import com.chachotkin.resource.service.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;
import java.util.Collections;

@FeignClient(
        name = "${com.chachotkin.service.storage.name}",
        url = "${com.chachotkin.service.api-gateway.url}",
        path = "/storages",
        configuration = FeignConfig.class,
        fallback = StorageServiceClient.Fallback.class
)
public interface StorageServiceClient {

    @GetMapping
    Collection<StorageDto> retrieveAll();

    default StorageDto retrieveStorageById(long storageId) {
        var storages = retrieveAll();
        return storages.stream()
                .filter(storageDto -> storageId == storageDto.getId())
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Storage with id [%d] doesn't exist!", storageId))
                );
    }

    default StorageDto retrieveStagingStorage() {
        var storages = retrieveAll();
        return storages.stream()
                .filter(storageDto -> StorageType.STAGING.equals(storageDto.getType()))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException("Staging storage not found!"));
    }

    default StorageDto retrievePermanentStorage() {
        var storages = retrieveAll();
        return storages.stream()
                .filter(storageDto -> StorageType.PERMANENT.equals(storageDto.getType()))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException("Permanent storage not found!"));
    }

    @Slf4j
    @Component
    class Fallback implements StorageServiceClient {

        @Override
        public Collection<StorageDto> retrieveAll() {
            log.warn("Failed to retrieve storages. Stub will be used instead.");
            return Collections.emptyList();
        }
    }
}
