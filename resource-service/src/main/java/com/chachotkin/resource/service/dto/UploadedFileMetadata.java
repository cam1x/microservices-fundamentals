package com.chachotkin.resource.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UploadedFileMetadata {

    @NonNull
    private String sourcePath;

    @NonNull
    private String eTag;
}
