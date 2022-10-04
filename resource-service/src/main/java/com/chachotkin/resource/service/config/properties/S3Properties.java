package com.chachotkin.resource.service.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@Getter
@Setter
@ConfigurationProperties(prefix = "s3")
public class S3Properties {

    private URI endpointURI;
}
