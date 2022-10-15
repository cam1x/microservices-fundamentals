package com.chachotkin.resource.processor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResourceMetadata implements Serializable {

    @NonNull
    private Long id;

    @NonNull
    private String sourcePath;

    @NonNull
    private String checksum;

    @NonNull
    private Long size;

    private LocalDateTime createdAt;
}
