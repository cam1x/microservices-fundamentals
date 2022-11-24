package com.chachotkin.resource.service;

import com.chachotkin.resource.service.config.properties.KafkaProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
@ContextConfiguration(classes = BaseIT.TestConfig.class, initializers = WireMockInitializer.class)
public abstract class BaseIT {

    protected static final String TOPIC_NAME = "resource-upload";

    @Container
    protected static final LocalStackContainer LOCALSTACK =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.2.0"))
                    .withServices(S3);

    @Container
    protected static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.10"));

    static {
        LOCALSTACK.start();
        KAFKA.start();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public S3Client amazonS3() {
            return S3Client.builder()
                    .endpointOverride(LOCALSTACK.getEndpointOverride(LocalStackContainer.Service.S3))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(LOCALSTACK.getAccessKey(), LOCALSTACK.getSecretKey())
                            )
                    )
                    .region(Region.of(LOCALSTACK.getRegion()))
                    .build();
        }

        @Bean
        @Primary
        public KafkaProperties kafkaProperties() {
            var kafkaProperties = new KafkaProperties();
            kafkaProperties.setBootstrapAddress(KAFKA.getBootstrapServers());
            kafkaProperties.setTopic(TOPIC_NAME);
            return kafkaProperties;
        }
    }

    @Autowired
    protected WireMockServer wireMockServer;

    @BeforeEach
    public void resetMocks() {
        WireMock.reset();
    }
}
