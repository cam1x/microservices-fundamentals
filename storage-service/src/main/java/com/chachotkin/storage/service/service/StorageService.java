package com.chachotkin.storage.service.service;

import com.chachotkin.storage.service.dto.DeleteResponseDto;
import com.chachotkin.storage.service.dto.StorageDto;
import com.chachotkin.storage.service.dto.UploadResponseDto;
import com.chachotkin.storage.service.entity.StorageEntity;
import com.chachotkin.storage.service.exception.ResourceNotFoundException;
import com.chachotkin.storage.service.mapper.StorageMapper;
import com.chachotkin.storage.service.repository.StorageRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageRepository storageRepository;
    private final StorageMapper storageMapper;

    public UploadResponseDto upload(@NonNull StorageDto storageDto) {
        var storageEntity = storageMapper.toEntity(storageDto);
        var savedStorage = storageRepository.save(storageEntity);
        return new UploadResponseDto(savedStorage.getId());
    }

    public Collection<StorageDto> findAll() {
        var storageEntities = storageRepository.findAll();
        return storageEntities.stream()
                .map(storageMapper::toDto)
                .collect(Collectors.toList());
    }

    public DeleteResponseDto delete(@NonNull Collection<Long> ids) {
        var storages = storageRepository.findAllById(ids);

        if (storages.isEmpty()) {
            throw new ResourceNotFoundException(String.format("No storages found for provided ids [%s]", ids));
        }

        var idsToDelete = storages.stream()
                .map(StorageEntity::getId)
                .toList();
        storageRepository.deleteAllById(idsToDelete);
        return new DeleteResponseDto(idsToDelete);
    }
}
