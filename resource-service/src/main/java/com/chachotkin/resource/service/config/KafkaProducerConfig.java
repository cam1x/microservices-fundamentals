package com.chachotkin.resource.service.config;

import com.chachotkin.resource.service.config.properties.KafkaProperties;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;

@Configuration
@EnableConfigurationProperties(value = KafkaProperties.class)
public class KafkaProducerConfig {

    @Bean
    public KafkaAdmin kafkaAdmin(KafkaProperties kafkaProperties) {
        var configs = new HashMap<String, Object>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapAddress());
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topic(KafkaProperties kafkaProperties) {
        return TopicBuilder.name(kafkaProperties.getTopic()).build();
    }

    @Bean
    public ProducerFactory<Long, String> producerFactory(KafkaProperties kafkaProperties) {
        var configProps = new HashMap<String, Object>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapAddress());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<Long, String> kafkaTemplate(ProducerFactory<Long, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
