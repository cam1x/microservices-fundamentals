package com.chachotkin.resource.service.service;

import com.chachotkin.resource.service.dto.DeleteResponseDto;
import com.chachotkin.resource.service.dto.UploadResponseDto;
import com.chachotkin.resource.service.entity.ResourceEntity;
import com.chachotkin.resource.service.exception.BadRequestException;
import com.chachotkin.resource.service.exception.ResourceAlreadyExistsException;
import com.chachotkin.resource.service.exception.ResourceNotFoundException;
import com.chachotkin.resource.service.repository.ResourceRepository;
import com.chachotkin.resource.service.util.RegexUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;

import static com.chachotkin.resource.service.util.AppConstants.AUDIO_CONTENT_TYPE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceService {

    private final S3Service s3Service;
    private final ResourcePublisher resourcePublisher;
    private final ResourceRepository resourceRepository;

    @Transactional
    public UploadResponseDto upload(@NonNull MultipartFile multipartFile) {
        if (!AUDIO_CONTENT_TYPE.equalsIgnoreCase(multipartFile.getContentType())) {
            throw new BadRequestException("Provided content type isn't supported!");
        }

        var uploadedFileMetadata = s3Service.uploadFile(multipartFile);
        var sourcePath = uploadedFileMetadata.getSourcePath();
        var eTag = uploadedFileMetadata.getETag();

        if (resourceRepository.findBySourcePathAndChecksum(sourcePath, eTag).isPresent()) {
            throw new ResourceAlreadyExistsException("Uploading resource already exists!");
        }

        var resource = ResourceEntity.builder()
                .size(multipartFile.getSize())
                .sourcePath(sourcePath)
                .checksum(eTag)
                .build();
        var resourceId = resourceRepository.save(resource).getId();
        resourcePublisher.publish(resource);
        return new UploadResponseDto(resourceId);
    }

    public byte[] donwload(@NonNull Long id, String range) {
        var resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        if (range == null) {
            return s3Service.downloadFile(resource.getSourcePath());
        }

        var matcher = RegexUtils.RANGE_VALUE.matcher(range);
        if (!matcher.matches()) {
            throw new BadRequestException("Provided byte range has invalid format!");
        }

        var start = Long.parseLong(matcher.group(1));
        var end = StringUtils.isNotEmpty(matcher.group(2)) ? Long.parseLong(matcher.group(2)) : resource.getSize();
        if (start < 0 || start > end || end > resource.getSize()) {
            throw new BadRequestException(String.format("Provided byte range is invalid for resource [%s]!", id));
        }

        return s3Service.downloadFile(resource.getSourcePath(), range);
    }

    public DeleteResponseDto delete(@NonNull Collection<Long> ids) {
        var resources = resourceRepository.findAllById(ids);

        if (resources.isEmpty()) {
            throw new ResourceNotFoundException(String.format("No resources found for provided ids [%s]", ids));
        }

        var deletedIds = new ArrayList<Long>();
        for (ResourceEntity resource : resources) {
            try {
                s3Service.deleteFile(resource.getSourcePath());
                resourceRepository.delete(resource);
                deletedIds.add(resource.getId());
            } catch (Exception e) {
                log.warn("Deletion of resource with id [{}] failed! Will continue processing of other resources...",
                        resource.getId());
            }
        }

        return new DeleteResponseDto(deletedIds);
    }
}
