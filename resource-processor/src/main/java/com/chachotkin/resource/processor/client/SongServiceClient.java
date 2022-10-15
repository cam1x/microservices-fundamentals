package com.chachotkin.resource.processor.client;

import com.chachotkin.resource.processor.config.FeignConfig;
import com.chachotkin.resource.processor.dto.SongMetadata;
import com.chachotkin.resource.processor.dto.UploadResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        value = "${com.chachotkin.service.song.name}",
        url = "${com.chachotkin.service.song.url}",
        configuration = FeignConfig.class
)
public interface SongServiceClient {

    @PostMapping("/songs")
    UploadResponseDto upload(@RequestBody SongMetadata songMetadata);
}
