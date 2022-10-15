package com.chachotkin.resource.service.service;

import com.chachotkin.resource.service.config.properties.KafkaProperties;
import com.chachotkin.resource.service.entity.ResourceEntity;
import com.chachotkin.resource.service.exception.ServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourcePublisher {

    private final KafkaTemplate<Long, String> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final PublishEventCallback publishEventCallback;
    private final ObjectMapper objectMapper;

    @PostConstruct
    void init() {
        kafkaTemplate.setDefaultTopic(kafkaProperties.getTopic());
    }

    public void publish(ResourceEntity resource) {
        try {
            var message = MessageBuilder.withPayload(objectMapper.writeValueAsString(resource))
                    .setHeader(KafkaHeaders.KEY, resource.getId())
                    .build();

            var future = kafkaTemplate.send(message);
            future.addCallback(publishEventCallback);
        } catch (JsonProcessingException e) {
            log.error("Failed to build message for event publishing: {}!", e.getMessage(), e);
            throw new ServiceException("Failed to build message for event publishing!");
        }
    }
}
