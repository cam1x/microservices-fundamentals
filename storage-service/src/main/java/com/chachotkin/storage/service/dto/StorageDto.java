package com.chachotkin.storage.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StorageDto {

    private Long id;

    @NotNull
    private StorageType type;

    @NotBlank
    private String bucket;

    @NotBlank
    private String path;
}
