package com.chachotkin.resource.service.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "audio.upload")
public class AudioUploadProperties {

    @NotEmpty
    private String bucket;
    private String directory;
}
