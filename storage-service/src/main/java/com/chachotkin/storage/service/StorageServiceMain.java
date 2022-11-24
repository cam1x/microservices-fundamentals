package com.chachotkin.storage.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class StorageServiceMain {

    public static void main(String[] args) {
        SpringApplication.run(StorageServiceMain.class, args);
    }
}
