package com.chachotkin.storage.service.controller;

import com.chachotkin.storage.service.dto.DeleteResponseDto;
import com.chachotkin.storage.service.dto.StorageDto;
import com.chachotkin.storage.service.dto.UploadResponseDto;
import com.chachotkin.storage.service.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.Collection;

@RequestMapping("/storages")
@RestController
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadResponseDto> upload(@RequestBody @Valid StorageDto storageDto) {
        return ResponseEntity.ok(storageService.upload(storageDto));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<StorageDto>> retrieveAll() {
        return ResponseEntity.ok(storageService.findAll());
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeleteResponseDto> delete(@RequestParam @Size(min = 1, max = 200) Collection<Long> ids) {
        var deletedIds = storageService.delete(ids);
        return deletedIds.getIds().size() == ids.size()
                ? ResponseEntity.ok(deletedIds)
                : ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(deletedIds);
    }
}
