package com.chachotkin.song.service.controller;

import com.chachotkin.song.service.dto.DeleteResponseDto;
import com.chachotkin.song.service.dto.SongDto;
import com.chachotkin.song.service.dto.UploadResponseDto;
import com.chachotkin.song.service.service.SongService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = SongController.class)
public class SongContractTestBase {

    private static final long SONG_ID = 1L;

    @MockBean
    private SongService songService;

    @Autowired
    private SongController songController;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.standaloneSetup(songController);

        var song = SongDto.builder()
                .name("We are the champions")
                .artist("Queen")
                .album("News of the world")
                .length("2:59")
                .resourceId(1L)
                .year(1977)
                .build();

        when(songService.upload(song)).thenReturn(new UploadResponseDto(SONG_ID));
        when(songService.retrieve(SONG_ID)).thenReturn(song);
        when(songService.delete(anyCollection()))
                .thenReturn(new DeleteResponseDto(Collections.singletonList(SONG_ID)));
    }
}
