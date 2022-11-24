package com.chachotkin.resource.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
public class WireMockInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        var wireMockServer = new WireMockServer(
                new WireMockConfiguration()
                        .dynamicPort()
                        .extensions(new ResponseTemplateTransformer(false))
        );

        wireMockServer.start();

        log.info("Starting wiremock server for application context [{}] with base url [{}]",
                applicationContext.getId(),
                wireMockServer.baseUrl());

        WireMock.configureFor(wireMockServer.port());

        applicationContext.getBeanFactory().registerSingleton("wireMockServer", wireMockServer);

        TestPropertyValues
                .of("com.chachotkin.service.api-gateway.url:http://localhost:" + wireMockServer.port())
                .applyTo(applicationContext);
    }
}
