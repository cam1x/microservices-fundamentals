package com.chachotkin.resource.service.config;

import com.chachotkin.resource.service.config.properties.S3Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(value = S3Properties.class)
public class S3ClientConfig {

    @Bean
    public S3Client s3Client(S3Properties s3Properties) {
        var s3ClientBuilder = S3Client.builder();

        if (s3Properties.getEndpointURI() != null) {
            s3ClientBuilder.endpointOverride(s3Properties.getEndpointURI());
        }

        return s3ClientBuilder
                .region(Region.US_EAST_1)
                .build();
    }
}
