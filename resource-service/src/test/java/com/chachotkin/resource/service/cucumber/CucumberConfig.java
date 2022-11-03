package com.chachotkin.resource.service.cucumber;

import com.chachotkin.resource.service.BaseIT;
import com.chachotkin.resource.service.cucumber.config.TestConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestConfig.class)
@CucumberContextConfiguration
public class CucumberConfig extends BaseIT {
}
