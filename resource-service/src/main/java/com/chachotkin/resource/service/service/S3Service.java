package com.chachotkin.resource.service.service;

import com.chachotkin.resource.service.dto.StorageDto;
import com.chachotkin.resource.service.exception.ServiceException;
import com.chachotkin.resource.service.util.RegexUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

    private static final String FORWARD_SLASH = "/";

    private final S3Client s3Client;

    public String uploadFile(@NonNull MultipartFile multipartFile, @NonNull StorageDto storageDto) {
        createBucketIfNotExists(storageDto.getBucket());

        var fileName = multipartFile.getOriginalFilename();
        var resourceKey = buildResourceKey(fileName, storageDto.getPath());

        var putObjectRequest = PutObjectRequest.builder()
                .bucket(storageDto.getBucket())
                .key(resourceKey)
                .build();
        try {
            var putObjectResponse = s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize())
            );
            return formatETag(putObjectResponse.eTag());
        } catch (IOException e) {
            throw new ServiceException(String.format("Failed to upload file [%s]!", fileName), e);
        }
    }

    public byte[] downloadFile(@NonNull String fileName, @NonNull StorageDto storageDto) {
        return downloadFile(fileName, storageDto, null);
    }

    public byte[] downloadFile(@NonNull String fileName, @NonNull StorageDto storageDto, String range) {
        var resourceKey = buildResourceKey(fileName, storageDto.getPath());

        var getObjectRequestBuilder = GetObjectRequest.builder()
                .bucket(storageDto.getBucket())
                .key(resourceKey)
                .range(range);

        if (range != null && RegexUtils.RANGE_VALUE.matcher(range).matches()) {
            getObjectRequestBuilder.range(range);
        }

        return s3Client.getObjectAsBytes(getObjectRequestBuilder.build()).asByteArray();
    }

    public void copyFile(@NonNull String fileName,
                         @NonNull StorageDto sourceStorage, @NonNull StorageDto destStorage) {
        createBucketIfNotExists(destStorage.getBucket());

        var copyObjectRequest = CopyObjectRequest.builder()
                .sourceBucket(sourceStorage.getBucket())
                .sourceKey(buildResourceKey(fileName, sourceStorage.getPath()))
                .destinationBucket(destStorage.getBucket())
                .destinationKey(buildResourceKey(fileName, destStorage.getPath()))
                .build();

        s3Client.copyObject(copyObjectRequest);
    }

    public void deleteFile(@NonNull String fileName, @NonNull StorageDto storageDto) {
        var resourceKey = buildResourceKey(fileName, storageDto.getPath());

        var deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(storageDto.getBucket())
                .key(resourceKey)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    private void createBucketIfNotExists(String bucketName) {
        if (isBucketExist(bucketName)) {
            return;
        }

        try {
            var createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.createBucket(createBucketRequest);
        } catch (S3Exception e) {
            log.warn("Failed to manually create bucket [{}]!", bucketName, e);
        }
    }

    private boolean isBucketExist(String bucketName) {
        var headBucketRequest = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();
        try {
            s3Client.headBucket(headBucketRequest);
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }

    private String buildResourceKey(String fileName, String uploadDirectory) {
        if (StringUtils.isEmpty(uploadDirectory)) {
            return fileName;
        }

        var formattedUploadDirectory = uploadDirectory.endsWith(FORWARD_SLASH)
                ? uploadDirectory.substring(0, uploadDirectory.length() - 1)
                : uploadDirectory;

        return formattedUploadDirectory + FORWARD_SLASH + fileName;
    }

    private String formatETag(String eTag) {
        return eTag != null && eTag.length() > 2 ? eTag.substring(1, eTag.length() - 1) : eTag;
    }
}
