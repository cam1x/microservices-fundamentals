package com.chachotkin.resource.service.cucumber.client;

import com.chachotkin.resource.service.controller.ResourceController;
import com.chachotkin.resource.service.service.ResourceService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @Autowired
    private WireMockServer wireMockServer;

    @PostConstruct
    private void init() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/storages"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("/response/storage.json"))
        );

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
