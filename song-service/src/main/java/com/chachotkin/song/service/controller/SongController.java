package com.chachotkin.song.service.controller;

import com.chachotkin.song.service.dto.DeleteResponseDto;
import com.chachotkin.song.service.dto.SongDto;
import com.chachotkin.song.service.dto.UploadResponseDto;
import com.chachotkin.song.service.service.SongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.Collection;

@Slf4j
@RequestMapping("/songs")
@RestController
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadResponseDto> upload(@RequestBody @Valid SongDto songDto) {
        log.info("Received request for uploading song: {}", songDto);
        return ResponseEntity.ok(songService.upload(songDto));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SongDto> retrieve(@PathVariable Long id) {
        log.info("Received request for retrieving song by id {}", id);
        return ResponseEntity.ok(songService.retrieve(id));
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeleteResponseDto> delete(@RequestParam @Size(min = 1, max = 200) Collection<Long> ids) {
        log.info("Received request for deleting songs by ids {}", ids);
        var deletedIds = songService.delete(ids);
        return deletedIds.getIds().size() == ids.size()
                ? ResponseEntity.ok(deletedIds)
                : ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(deletedIds);
    }
}
