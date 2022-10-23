package com.chachotkin.gateway.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class ApiGatewayServiceMain {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayServiceMain.class, args);
    }
}
