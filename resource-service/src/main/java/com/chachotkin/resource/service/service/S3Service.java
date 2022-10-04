package com.chachotkin.resource.service.service;

import com.chachotkin.resource.service.config.properties.AudioUploadProperties;
import com.chachotkin.resource.service.dto.UploadedFileMetadata;
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
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

    private static final String FORWARD_SLASH = "/";

    private final S3Client s3Client;
    private final AudioUploadProperties audioUploadProperties;

    @PostConstruct
    public void createAudioBucketIfNotExists() {
        var bucketName = audioUploadProperties.getBucket();

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

    public UploadedFileMetadata uploadFile(@NonNull MultipartFile multipartFile) {
        var fileName = multipartFile.getOriginalFilename();
        var resourceKey = buildResourceKey(fileName);

        var putObjectRequest = PutObjectRequest.builder()
                .bucket(audioUploadProperties.getBucket())
                .key(resourceKey)
                .build();
        try {
            var putObjectResponse = s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize())
            );
            return UploadedFileMetadata.builder()
                    .sourcePath(String.format("s3://%s/%s", audioUploadProperties.getBucket(), resourceKey))
                    .eTag(formatETag(putObjectResponse.eTag()))
                    .build();
        } catch (IOException e) {
            throw new ServiceException(String.format("Failed to upload file [%s]!", fileName), e);
        }
    }

    public byte[] downloadFile(@NonNull String sourcePath) {
        return downloadFile(sourcePath, null);
    }

    public byte[] downloadFile(@NonNull String sourcePath, String range) {
        var pathMatcher = RegexUtils.S3_SOURCE_PATH.matcher(sourcePath);
        if (!pathMatcher.matches()) {
            throw new ServiceException(String.format("Source path [%s] is invalid for s3 resource!", sourcePath));
        }

        var getObjectRequestBuilder = GetObjectRequest.builder()
                .bucket(pathMatcher.group(1))
                .key(pathMatcher.group(2))
                .range(range);

        if (range != null && RegexUtils.RANGE_VALUE.matcher(range).matches()) {
            getObjectRequestBuilder.range(range);
        }

        return s3Client.getObjectAsBytes(getObjectRequestBuilder.build()).asByteArray();
    }

    public void deleteFile(@NonNull String sourcePath) {
        var pathMatcher = RegexUtils.S3_SOURCE_PATH.matcher(sourcePath);
        if (!pathMatcher.matches()) {
            throw new ServiceException(String.format("Source path [%s] is invalid for s3 resource!", sourcePath));
        }

        var deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(pathMatcher.group(1))
                .key(pathMatcher.group(2))
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    private String buildResourceKey(String fileName) {
        var uploadDirectory = audioUploadProperties.getDirectory();

        if (StringUtils.isEmpty(uploadDirectory)) {
            return fileName;
        }

        var formattedUploadDirectory = uploadDirectory.endsWith(FORWARD_SLASH)
                ? uploadDirectory.substring(0, uploadDirectory.length() - 1)
                : uploadDirectory;

        return formattedUploadDirectory + FORWARD_SLASH + fileName;
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

    private String formatETag(String eTag) {
        return eTag != null && eTag.length() > 2 ? eTag.substring(1, eTag.length() - 1) : eTag;
    }
}
