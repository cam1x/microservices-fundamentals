package com.chachotkin.resource.service.service;

import com.chachotkin.resource.service.dto.UploadedFileMetadata;
import com.chachotkin.resource.service.entity.ResourceEntity;
import com.chachotkin.resource.service.exception.BadRequestException;
import com.chachotkin.resource.service.exception.ResourceAlreadyExistsException;
import com.chachotkin.resource.service.exception.ResourceNotFoundException;
import com.chachotkin.resource.service.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.chachotkin.resource.service.util.AppConstants.AUDIO_CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourceServiceTest {

    private static final long RESOURCE_ID = 1L;
    private static final long FILE_SIZE = 100L;
    private static final byte[] FILE_CONTENT = new byte[100];
    private static final String SOURCE_PATH = "s3://resources/audio/file_example_2MB.mp3";
    private static final String ETAG = "8c97039fdb854de770a4e0bbceea043d";

    @Mock
    private S3Service s3Service;

    @Mock
    private ResourcePublisher resourcePublisher;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceService resourceService;

    @Mock
    private MultipartFile multipartFile;

    @Test
    void shouldFailUploadForNotAudioResource() {
        // given
        when(multipartFile.getContentType()).thenReturn("application/json");

        // when and then
        assertThrows(BadRequestException.class, () -> resourceService.upload(multipartFile));
        verifyNoInteractions(s3Service, resourceRepository, resourcePublisher);
    }

    @Test
    void shouldFailUploadForDuplicatedResource() {
        // given
        when(multipartFile.getContentType()).thenReturn(AUDIO_CONTENT_TYPE);

        var uploadedFileMetadata = UploadedFileMetadata.builder()
                .sourcePath(SOURCE_PATH)
                .eTag(ETAG)
                .build();
        when(s3Service.uploadFile(multipartFile)).thenReturn(uploadedFileMetadata);
        when(resourceRepository.findBySourcePathAndChecksum(SOURCE_PATH, ETAG))
                .thenReturn(Optional.of(ResourceEntity.builder().build()));

        // when and then
        assertThrows(ResourceAlreadyExistsException.class, () -> resourceService.upload(multipartFile));
        verify(s3Service).uploadFile(multipartFile);
        verify(resourceRepository).findBySourcePathAndChecksum(SOURCE_PATH, ETAG);
        verifyNoMoreInteractions(s3Service, resourceRepository);
        verifyNoInteractions(resourcePublisher);
    }

    @Test
    void shouldUploadResource() {
        // given
        when(multipartFile.getContentType()).thenReturn(AUDIO_CONTENT_TYPE);
        when(multipartFile.getSize()).thenReturn(FILE_SIZE);

        var uploadedFileMetadata = UploadedFileMetadata.builder()
                .sourcePath(SOURCE_PATH)
                .eTag(ETAG)
                .build();
        when(s3Service.uploadFile(multipartFile)).thenReturn(uploadedFileMetadata);
        when(resourceRepository.findBySourcePathAndChecksum(SOURCE_PATH, ETAG)).thenReturn(Optional.empty());

        var resourceEntity = ResourceEntity.builder()
                .size(FILE_SIZE)
                .sourcePath(SOURCE_PATH)
                .checksum(ETAG)
                .build();
        var savedResource = resourceEntity.toBuilder()
                .id(RESOURCE_ID)
                .build();
        when(resourceRepository.save(resourceEntity)).thenReturn(savedResource);

        // when
        var uploadResponseDto = resourceService.upload(multipartFile);

        // then
        assertEquals(RESOURCE_ID, uploadResponseDto.getId());
        verify(multipartFile).getContentType();
        verify(s3Service).uploadFile(multipartFile);
        verify(resourceRepository).findBySourcePathAndChecksum(SOURCE_PATH, ETAG);
        verify(resourceRepository).save(resourceEntity);
        verify(resourcePublisher).publish(savedResource);
        verifyNoMoreInteractions(s3Service, resourceRepository, resourcePublisher);
    }

    @Test
    void shouldFailDownloadOfNotExistedResource() {
        // given
        when(resourceRepository.findById(RESOURCE_ID)).thenReturn(Optional.empty());

        // when and then
        assertThrows(ResourceNotFoundException.class, () -> resourceService.donwload(RESOURCE_ID, null));
        verify(resourceRepository).findById(RESOURCE_ID);
        verifyNoInteractions(s3Service, resourcePublisher);
    }

    @Test
    void shouldDownloadTheWholeResource() {
        // given
        var resourceEntity = ResourceEntity.builder()
                .size(FILE_SIZE)
                .sourcePath(SOURCE_PATH)
                .checksum(ETAG)
                .build();
        when(resourceRepository.findById(RESOURCE_ID)).thenReturn(Optional.of(resourceEntity));
        when(s3Service.downloadFile(SOURCE_PATH)).thenReturn(FILE_CONTENT);

        // when
        var downloaded = resourceService.donwload(RESOURCE_ID, null);

        // then
        assertEquals(FILE_CONTENT, downloaded);
        verify(resourceRepository).findById(RESOURCE_ID);
        verify(s3Service).downloadFile(SOURCE_PATH);
        verifyNoMoreInteractions(resourceRepository, s3Service);
        verifyNoInteractions(resourcePublisher);
    }

    @Test
    void shouldFailDownloadWithInvalidRangeFormat() {
        // given
        var resourceEntity = ResourceEntity.builder()
                .size(FILE_SIZE)
                .sourcePath(SOURCE_PATH)
                .checksum(ETAG)
                .build();
        when(resourceRepository.findById(RESOURCE_ID)).thenReturn(Optional.of(resourceEntity));

        // when and then
        assertThrows(BadRequestException.class, () -> resourceService.donwload(RESOURCE_ID, "fake-range"));
        verify(resourceRepository).findById(RESOURCE_ID);
        verifyNoMoreInteractions(resourceRepository);
        verifyNoInteractions(s3Service, resourcePublisher);
    }

    @Test
    void shouldFailDownloadWithInvalidRangeBoundaries() {
        // given
        var resourceEntity = ResourceEntity.builder()
                .size(FILE_SIZE)
                .sourcePath(SOURCE_PATH)
                .checksum(ETAG)
                .build();
        when(resourceRepository.findById(RESOURCE_ID)).thenReturn(Optional.of(resourceEntity));

        // when and then
        assertThrows(BadRequestException.class, () -> resourceService.donwload(RESOURCE_ID, "bytes=1-100000"));
        verify(resourceRepository).findById(RESOURCE_ID);
        verifyNoMoreInteractions(resourceRepository);
        verifyNoInteractions(s3Service, resourcePublisher);
    }

    @Test
    void shouldDownloadPartOfResource() {
        // given
        var resourceEntity = ResourceEntity.builder()
                .size(FILE_SIZE)
                .sourcePath(SOURCE_PATH)
                .checksum(ETAG)
                .build();
        when(resourceRepository.findById(RESOURCE_ID)).thenReturn(Optional.of(resourceEntity));
        var range = "bytes=1-50";
        when(s3Service.downloadFile(SOURCE_PATH, range)).thenReturn(FILE_CONTENT);

        // when
        var downloaded = resourceService.donwload(RESOURCE_ID, "bytes=1-50");

        // then
        assertEquals(FILE_CONTENT, downloaded);
        verify(resourceRepository).findById(RESOURCE_ID);
        verify(s3Service).downloadFile(SOURCE_PATH, range);
        verifyNoMoreInteractions(resourceRepository, s3Service);
        verifyNoInteractions(resourcePublisher);
    }

    @Test
    void shouldFailDeleteOfNotExistedResource() {
        // given
        var ids = List.of(RESOURCE_ID);
        when(resourceRepository.findAllById(ids)).thenReturn(Collections.emptyList());

        // when and then
        assertThrows(ResourceNotFoundException.class, () -> resourceService.delete(ids));
        verify(resourceRepository).findAllById(ids);
        verifyNoMoreInteractions(resourceRepository);
        verifyNoInteractions(s3Service, resourcePublisher);
    }

    @Test
    void shouldDeleteResource() {
        // given
        var ids = List.of(RESOURCE_ID);
        var resourceEntity = ResourceEntity.builder()
                .id(RESOURCE_ID)
                .size(FILE_SIZE)
                .sourcePath(SOURCE_PATH)
                .checksum(ETAG)
                .build();
        when(resourceRepository.findAllById(ids)).thenReturn(List.of(resourceEntity));

        // when
        var deleteResponse = resourceService.delete(ids);

        // then
        assertEquals(ids, deleteResponse.getIds());
        verify(s3Service).deleteFile(SOURCE_PATH);
        verify(resourceRepository).delete(resourceEntity);
        verifyNoMoreInteractions(s3Service, resourceRepository);
        verifyNoInteractions(resourcePublisher);
    }
}
