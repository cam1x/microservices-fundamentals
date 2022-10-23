package com.chachotkin.resource.processor.client;

import com.chachotkin.resource.processor.config.FeignConfig;
import com.chachotkin.resource.processor.dto.SongMetadata;
import com.chachotkin.resource.processor.dto.UploadResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "${com.chachotkin.service.song.name}",
        url = "${com.chachotkin.service.api-gateway.url}",
        path = "/songs",
        configuration = FeignConfig.class
)
public interface SongServiceClient {

    @PostMapping
    UploadResponseDto upload(@RequestBody SongMetadata songMetadata);
}
