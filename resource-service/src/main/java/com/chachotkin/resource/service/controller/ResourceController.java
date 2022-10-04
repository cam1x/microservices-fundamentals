package com.chachotkin.resource.service.controller;

import com.chachotkin.resource.service.dto.DeleteResponseDto;
import com.chachotkin.resource.service.dto.UploadResponseDto;
import com.chachotkin.resource.service.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Size;
import java.util.Collection;

import static com.chachotkin.resource.service.util.AppConstants.AUDIO_CONTENT_TYPE;

@RequestMapping("/resources")
@RestController
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDto> upload(@RequestParam("file") MultipartFile multipartFile) {
        return ResponseEntity.ok(resourceService.upload(multipartFile));
    }

    @GetMapping(value = "/{id}", produces = AUDIO_CONTENT_TYPE)
    public ResponseEntity<ByteArrayResource> download(@PathVariable Long id,
                                                      @RequestHeader(value = HttpHeaders.RANGE, required = false) String range) {
        var data = resourceService.donwload(id, range);
        return ResponseEntity.ok()
                .contentLength(data.length)
                .header(HttpHeaders.CONTENT_TYPE, AUDIO_CONTENT_TYPE)
                .body(new ByteArrayResource(data));
    }

    @DeleteMapping
    public ResponseEntity<DeleteResponseDto> delete(@RequestParam @Size(min = 1, max = 200) Collection<Long> ids) {
        var deletedIds = resourceService.delete(ids);
        return deletedIds.getIds().size() == ids.size()
                ? ResponseEntity.ok(deletedIds)
                : ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(deletedIds);
    }
}
