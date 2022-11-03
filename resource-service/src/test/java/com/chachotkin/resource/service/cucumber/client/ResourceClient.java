package com.chachotkin.resource.service.cucumber.client;

import com.chachotkin.resource.service.controller.ResourceController;
import com.chachotkin.resource.service.service.ResourceService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;

import static com.chachotkin.resource.service.util.AppConstants.AUDIO_CONTENT_TYPE;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

@Component
@RequiredArgsConstructor
public class ResourceClient {

    private static final String RESOURCES_BASE_PATH = "/resources";

    @Autowired
    private ResourceService resourceService;

    @PostConstruct
    private void init() {
        RestAssuredMockMvc.standaloneSetup(new ResourceController(resourceService));
    }

    public MockMvcResponse uploadResource(InputStream inputStream, String fileName) {
        return given()
                .multiPart("file", fileName, inputStream, AUDIO_CONTENT_TYPE)
                .post(RESOURCES_BASE_PATH);
    }

    public MockMvcResponse downloadResource(long resourceId) {
        return given().get(RESOURCES_BASE_PATH + "/{id}", resourceId);
    }

    public MockMvcResponse deleteResource(long resourceId) {
        return given().delete(RESOURCES_BASE_PATH + "?ids=" + resourceId);
    }
}
