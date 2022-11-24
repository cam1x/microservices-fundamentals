package com.chachotkin.resource.service.service;

import com.chachotkin.resource.service.client.StorageServiceClient;
import com.chachotkin.resource.service.dto.StorageDto;
import com.chachotkin.resource.service.dto.StorageType;
import com.chachotkin.resource.service.entity.ResourceEntity;
import com.chachotkin.resource.service.exception.BadRequestException;
import com.chachotkin.resource.service.exception.ResourceAlreadyExistsException;
import com.chachotkin.resource.service.exception.ResourceNotFoundException;
import com.chachotkin.resource.service.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
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
    private static final String FILE_NAME = "file";
    private static final byte[] FILE_CONTENT = new byte[100];
    private static final String ETAG = "8c97039fdb854de770a4e0bbceea043d";

    @Mock
    private S3Service s3Service;

    @Mock
    private StorageServiceClient storageServiceClient;

    @Mock
    private ResourcePublisher resourcePublisher;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceService resourceService;

    @Mock
    private MultipartFile multipartFile;

    private StorageDto stagingStorage;

    @BeforeEach
    void setUp() {
        stagingStorage = StorageDto.builder()
                .id(1L)
                .bucket("test-bucket")
                .path("files")
                .type(StorageType.STAGING)
                .build();
    }

    @Test
    void shouldFailUploadForNotAudioResource() {
        // given
        when(multipartFile.getContentType()).thenReturn("application/json");

        // when and then
        assertThrows(BadRequestException.class, () -> resourceService.upload(multipartFile));
        verifyNoInteractions(storageServiceClient, s3Service, resourceRepository, resourcePublisher);
    }

    @Test
    void shouldFailUploadForDuplicatedResource() {
        // given
        when(multipartFile.getContentType()).thenReturn(AUDIO_CONTENT_TYPE);

        when(storageServiceClient.retrieveStagingStorage()).thenReturn(stagingStorage);
        when(s3Service.uploadFile(multipartFile, stagingStorage)).thenReturn(ETAG);
        when(resourceRepository.findByStorageIdAndChecksum(stagingStorage.getId(), ETAG))
                .thenReturn(Optional.of(ResourceEntity.builder().build()));

        // when and then
        assertThrows(ResourceAlreadyExistsException.class, () -> resourceService.upload(multipartFile));
        verify(storageServiceClient).retrieveStagingStorage();
        verify(s3Service).uploadFile(multipartFile, stagingStorage);
        verify(resourceRepository).findByStorageIdAndChecksum(stagingStorage.getId(), ETAG);
        verifyNoMoreInteractions(storageServiceClient, s3Service, resourceRepository);
        verifyNoInteractions(resourcePublisher);
    }

    @Test
    void shouldUploadResource() {
        // given
        when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
        when(multipartFile.getContentType()).thenReturn(AUDIO_CONTENT_TYPE);
        when(multipartFile.getSize()).thenReturn(FILE_SIZE);

        when(storageServiceClient.retrieveStagingStorage()).thenReturn(stagingStorage);
        when(s3Service.uploadFile(multipartFile, stagingStorage)).thenReturn(ETAG);
        when(resourceRepository.findByStorageIdAndChecksum(stagingStorage.getId(), ETAG))
                .thenReturn(Optional.empty());

        var resourceEntity = ResourceEntity.builder()
                .size(FILE_SIZE)
                .fileName(FILE_NAME)
                .storageId(stagingStorage.getId())
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
        verify(storageServiceClient).retrieveStagingStorage();
        verify(s3Service).uploadFile(multipartFile, stagingStorage);
        verify(resourceRepository).findByStorageIdAndChecksum(stagingStorage.getId(), ETAG);
        verify(resourceRepository).save(resourceEntity);
        verify(resourcePublisher).publish(savedResource);
        verifyNoMoreInteractions(storageServiceClient, s3Service, resourceRepository, resourcePublisher);
    }

    @Test
    void shouldFailDownloadOfNotExistedResource() {
        // given
        when(resourceRepository.findById(RESOURCE_ID)).thenReturn(Optional.empty());

        // when and then
        assertThrows(ResourceNotFoundException.class, () -> resourceService.download(RESOURCE_ID, null));
        verify(resourceRepository).findById(RESOURCE_ID);
        verifyNoInteractions(storageServiceClient, s3Service, resourcePublisher);
    }

    @Test
    void shouldDownloadTheWholeResource() {
        // given
        var resourceEntity = ResourceEntity.builder()
                .size(FILE_SIZE)
                .fileName(FILE_NAME)
                .storageId(stagingStorage.getId())
                .checksum(ETAG)
                .build();
        when(resourceRepository.findById(RESOURCE_ID)).thenReturn(Optional.of(resourceEntity));
        when(storageServiceClient.retrieveStorageById(stagingStorage.getId())).thenReturn(stagingStorage);
        when(s3Service.downloadFile(FILE_NAME, stagingStorage)).thenReturn(FILE_CONTENT);

        // when
        var downloaded = resourceService.download(RESOURCE_ID, null);

        // then
        assertEquals(FILE_CONTENT, downloaded);
        verify(resourceRepository).findById(RESOURCE_ID);
        verify(storageServiceClient).retrieveStorageById(stagingStorage.getId());
        verify(s3Service).downloadFile(FILE_NAME, stagingStorage);
        verifyNoMoreInteractions(storageServiceClient, resourceRepository, s3Service);
        verifyNoInteractions(resourcePublisher);
    }

    @Test
    void shouldFailDownloadWithInvalidRangeFormat() {
        // given
        var resourceEntity = ResourceEntity.builder()
                .size(FILE_SIZE)
                .fileName(FILE_NAME)
                .storageId(stagingStorage.getId())
                .checksum(ETAG)
                .build();
        when(resourceRepository.findById(RESOURCE_ID)).thenReturn(Optional.of(resourceEntity));

        // when and then
        assertThrows(BadRequestException.class, () -> resourceService.download(RESOURCE_ID, "fake-range"));
        verify(resourceRepository).findById(RESOURCE_ID);
        verifyNoMoreInteractions(resourceRepository);
        verifyNoInteractions(storageServiceClient, s3Service, resourcePublisher);
    }

    @Test
    void shouldFailDownloadWithInvalidRangeBoundaries() {
        // given
        var resourceEntity = ResourceEntity.builder()
                .size(FILE_SIZE)
                .fileName(FILE_NAME)
                .storageId(stagingStorage.getId())
                .checksum(ETAG)
                .build();
        when(resourceRepository.findById(RESOURCE_ID)).thenReturn(Optional.of(resourceEntity));

        // when and then
        assertThrows(BadRequestException.class, () -> resourceService.download(RESOURCE_ID, "bytes=1-100000"));
        verify(resourceRepository).findById(RESOURCE_ID);
        verifyNoMoreInteractions(resourceRepository);
        verifyNoInteractions(storageServiceClient, s3Service, resourcePublisher);
    }

    @Test
    void shouldDownloadPartOfResource() {
        // given
        var resourceEntity = ResourceEntity.builder()
                .size(FILE_SIZE)
                .fileName(FILE_NAME)
                .storageId(stagingStorage.getId())
                .checksum(ETAG)
                .build();
        when(resourceRepository.findById(RESOURCE_ID)).thenReturn(Optional.of(resourceEntity));
        when(storageServiceClient.retrieveStorageById(stagingStorage.getId())).thenReturn(stagingStorage);
        var range = "bytes=1-50";
        when(s3Service.downloadFile(FILE_NAME, stagingStorage, range)).thenReturn(FILE_CONTENT);

        // when
        var downloaded = resourceService.download(RESOURCE_ID, "bytes=1-50");

        // then
        assertEquals(FILE_CONTENT, downloaded);
        verify(resourceRepository).findById(RESOURCE_ID);
        verify(storageServiceClient).retrieveStorageById(stagingStorage.getId());
        verify(s3Service).downloadFile(FILE_NAME, stagingStorage, range);
        verifyNoMoreInteractions(storageServiceClient, resourceRepository, s3Service);
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
                .fileName(FILE_NAME)
                .storageId(stagingStorage.getId())
                .checksum(ETAG)
                .build();
        when(resourceRepository.findAllById(ids)).thenReturn(List.of(resourceEntity));
        when(storageServiceClient.retrieveStorageById(stagingStorage.getId())).thenReturn(stagingStorage);

        // when
        var deleteResponse = resourceService.delete(ids);

        // then
        assertEquals(ids, deleteResponse.getIds());
        verify(storageServiceClient).retrieveStorageById(stagingStorage.getId());
        verify(s3Service).deleteFile(FILE_NAME, stagingStorage);
        verify(resourceRepository).delete(resourceEntity);
        verifyNoMoreInteractions(storageServiceClient, s3Service, resourceRepository);
        verifyNoInteractions(resourcePublisher);
    }
}
