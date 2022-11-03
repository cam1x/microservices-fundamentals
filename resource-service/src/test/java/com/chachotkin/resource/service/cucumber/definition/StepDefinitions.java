package com.chachotkin.resource.service.cucumber.definition;

import com.chachotkin.resource.service.cucumber.client.ResourceClient;
import com.chachotkin.resource.service.dto.DeleteResponseDto;
import com.chachotkin.resource.service.dto.UploadResponseDto;
import com.chachotkin.resource.service.entity.ResourceEntity;
import com.chachotkin.resource.service.repository.ResourceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.testcontainers.shaded.org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class StepDefinitions {

    private static final String MEDIA_FILES_BASE_PATH = "media/";

    private final ResourceClient resourceClient;
    private final ResourceRepository resourceRepository;
    private final ObjectMapper objectMapper;

    private final Collection<Long> createdResourcesIds = new ArrayList<>();

    private MockMvcResponse response;
    private UploadResponseDto uploadResponseDto;
    private DeleteResponseDto deleteResponseDto;

    @When("User uploads file {string}")
    public void userUploadsFile(String filePath) {
        try (InputStream is = new ClassPathResource(MEDIA_FILES_BASE_PATH + filePath).getInputStream()) {
            response = resourceClient.uploadResource(is, FilenameUtils.getName(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
            uploadResponseDto = response.as(new TypeRef<>() {
            });
            createdResourcesIds.add(uploadResponseDto.getId());
        }
    }

    @Then("Application responds with status {int}")
    public void applicationResponseStatusIs(int responseStatus) {
        assertThat(response.getStatusCode()).isEqualTo(responseStatus);
    }

    @And("Upload response is")
    public void uploadResponseIs(String jsonResponse) throws JsonProcessingException {
        var expectedContent =
                objectMapper.readValue(jsonResponse, new TypeReference<UploadResponseDto>() {
                });
        assertThat(uploadResponseDto.getId()).isEqualTo(expectedContent.getId());
    }

    @And("Delete response is")
    public void deleteResponseIs(String jsonResponse) throws JsonProcessingException {
        var expectedContent =
                objectMapper.readValue(jsonResponse, new TypeReference<DeleteResponseDto>() {
                });
        assertThat(deleteResponseDto.getIds()).isEqualTo(expectedContent.getIds());
    }

    @And("The following resources are stored in the system")
    public void theFollowingResourceWasPersistedInTheSystem(List<ResourceMetadata> resources) {
        resources.forEach(resourceMetadata -> {
                    Optional<ResourceEntity> foundResource = resourceRepository.findById(resourceMetadata.getId());
                    assertThat(foundResource).isPresent();
                    assertThat(foundResource.get().getSourcePath().endsWith(resourceMetadata.getFileName())).isTrue();
                }
        );
    }

    @Given("The following resources exist in the system")
    public void theFollowingResourceExistsInTheSystem(List<ResourceMetadata> resources) {
        resources.forEach(resourceMetadata -> {
                    userUploadsFile(resourceMetadata.getFileName());
                    assertThat(createdResourcesIds).contains(resourceMetadata.getId());
                }
        );
    }

    @When("User downloads resource with id={long}")
    public void userDownloadsResourceWithId(long resourceId) {
        response = resourceClient.downloadResource(resourceId);
    }

    @And("Response content type is {string}")
    public void responseContentTypeIs(String contentType) {
        assertThat(response.getContentType()).isEqualTo(contentType);
    }

    @And("Response contains file with size {long}")
    public void responseContainsFile(long fileSize) {
        assertThat(response.asByteArray().length).isEqualTo(fileSize);
    }

    @When("User deletes resource with id={long}")
    public void userDeletesResourceWithId(long resourceId) {
        response = resourceClient.deleteResource(resourceId);
        deleteResponseDto = response.as(new TypeRef<>() {
        });
    }
}
