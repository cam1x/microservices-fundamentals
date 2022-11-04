package com.chachotkin.resource.processor.client.contract;

import com.chachotkin.resource.processor.dto.SongMetadata;
import com.chachotkin.resource.processor.dto.UploadResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureStubRunner(
        stubsMode = StubRunnerProperties.StubsMode.LOCAL,
        ids = "com.chachotkin:song-service:+:stubs:8082")
@SpringBootTest
public class SongClientContractTest {

    private static final String SONG_BASE_URI = "http://localhost:8082/songs";
    private static final int SONG_ID = 1;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    void pingStub() {
        ResponseEntity<Void> response =
                restTemplate.getForEntity("http://localhost:8082/ping", Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void uploadSongMetadata() {
        // given
        var songMetadata = SongMetadata.builder()
                .album("News of the world")
                .artist("Queen")
                .name("We are the champions")
                .length("2:59")
                .resourceId(1L)
                .year(1977)
                .build();

        // when
        var response =
                restTemplate.postForEntity(SONG_BASE_URI, songMetadata, UploadResponseDto.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(SONG_ID);
    }

    @Test
    void retrieveSongMetadata() {
        // given
        var songMetadata = SongMetadata.builder()
                .album("News of the world")
                .artist("Queen")
                .name("We are the champions")
                .length("2:59")
                .resourceId(1L)
                .year(1977)
                .build();

        // when
        var response =
                restTemplate.getForEntity(SONG_BASE_URI + "/" + SONG_ID, SongMetadata.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(songMetadata);
    }
}
