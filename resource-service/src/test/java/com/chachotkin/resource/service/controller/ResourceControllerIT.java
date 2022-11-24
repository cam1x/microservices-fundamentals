package com.chachotkin.resource.service.controller;

import com.chachotkin.resource.service.BaseIT;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static com.chachotkin.resource.service.util.AppConstants.AUDIO_CONTENT_TYPE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class ResourceControllerIT extends BaseIT {

    private static final String RESOURCES_URL = "/resources";

    private static final String MEDIA_FILE_PATH = "/media/file_example_2MB.mp3";

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/storages"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("/response/storage.json"))
        );
    }

    @Test
    @Transactional
    void shouldUploadResource() throws Exception {
        // given
        var classPathResource = new ClassPathResource(MEDIA_FILE_PATH);
        var multipartFile = new MockMultipartFile(
                "file",
                "file_example_2MB.mp3",
                AUDIO_CONTENT_TYPE,
                classPathResource.getInputStream().readAllBytes()
        );

        // when
        mockMvc.perform(MockMvcRequestBuilders.multipart(RESOURCES_URL).file(multipartFile))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").isNotEmpty());
    }

    @Test
    @Transactional
    void shouldDownloadUploadedResource() throws Exception {
        // given
        var classPathResource = new ClassPathResource(MEDIA_FILE_PATH);
        var fileContent = classPathResource.getInputStream().readAllBytes();
        var multipartFile = new MockMultipartFile(
                "file",
                "file_example_2MB.mp3",
                AUDIO_CONTENT_TYPE,
                fileContent
        );

        var uploadResult = mockMvc.perform(MockMvcRequestBuilders.multipart(RESOURCES_URL).file(multipartFile))
                .andReturn();
        var uploadResponseContent = uploadResult.getResponse().getContentAsString();
        var resourceId = JsonPath.read(uploadResponseContent, "$.id");

        // when
        mockMvc
                .perform(MockMvcRequestBuilders.get(RESOURCES_URL + "/{id}", resourceId))
                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(AUDIO_CONTENT_TYPE))
                .andReturn();
    }

    @Test
    @Transactional
    void shouldDeleteUploadedResource() throws Exception {
        // given
        var classPathResource = new ClassPathResource(MEDIA_FILE_PATH);
        var fileContent = classPathResource.getInputStream().readAllBytes();
        var multipartFile = new MockMultipartFile(
                "file",
                "file_example_2MB.mp3",
                AUDIO_CONTENT_TYPE,
                fileContent
        );

        var uploadResult = mockMvc.perform(MockMvcRequestBuilders.multipart(RESOURCES_URL).file(multipartFile))
                .andReturn();
        var uploadResponseContent = uploadResult.getResponse().getContentAsString();
        var resourceId = JsonPath.read(uploadResponseContent, "$.id");

        // when
        mockMvc
                .perform(MockMvcRequestBuilders.delete(RESOURCES_URL).param("ids", resourceId.toString()))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath(".ids[0]").value(resourceId));
    }
}
