package com.chachotkin.resource.processor.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    private String groupId;
    private String bootstrapAddress;
    private String topic;
    private Map<String, String> properties;
}
