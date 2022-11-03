package com.chachotkin.resource.service.service;

import com.chachotkin.resource.service.BaseIT;
import com.chachotkin.resource.service.entity.ResourceEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ResourcePublisherIT extends BaseIT {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourcePublisher resourcePublisher;

    private Consumer<Long, String> consumer;

    @BeforeEach
    void setUp() {
        var consumerProps = new HashMap<String, Object>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumer = new DefaultKafkaConsumerFactory<Long, String>(consumerProps).createConsumer();
        consumer.subscribe(Collections.singletonList(TOPIC_NAME));
    }

    @Test
    @Transactional
    void shouldPublishResource() throws JsonProcessingException {
        // given
        var resource = ResourceEntity.builder()
                .id(1L)
                .checksum("8c97039fdb854de770a4e0bbceea043d")
                .sourcePath("s3://resources/audio/file_example_2MB.mp3")
                .size(100L)
                .createdAt(LocalDateTime.now())
                .build();

        // when and then
        assertDoesNotThrow(() -> resourcePublisher.publish(resource));

        var record = KafkaTestUtils.getSingleRecord(consumer, TOPIC_NAME);
        assertNotNull(record);
        assertEquals(resource.getId(), record.key());
        assertEquals(objectMapper.writeValueAsString(resource), record.value());
    }
}
