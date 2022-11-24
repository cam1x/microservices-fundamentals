package com.chachotkin.storage.service.mapper;

import com.chachotkin.storage.service.dto.StorageDto;
import com.chachotkin.storage.service.entity.StorageEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StorageMapper {

    StorageDto toDto(StorageEntity storageEntity);

    StorageEntity toEntity(StorageDto storageDto);
}
