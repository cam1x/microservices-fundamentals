package com.chachotkin.resource.service.config;

import com.chachotkin.resource.service.config.properties.AudioUploadProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = AudioUploadProperties.class)
public class AppConfig {

}
