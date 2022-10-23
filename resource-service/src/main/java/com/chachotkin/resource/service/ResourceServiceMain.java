package com.chachotkin.resource.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class ResourceServiceMain {

    public static void main(String[] args) {
        SpringApplication.run(ResourceServiceMain.class, args);
    }
}
