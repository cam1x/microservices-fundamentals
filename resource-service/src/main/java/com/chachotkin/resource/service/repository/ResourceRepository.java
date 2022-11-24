package com.chachotkin.resource.service.repository;

import com.chachotkin.resource.service.entity.ResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResourceRepository extends JpaRepository<ResourceEntity, Long> {

    Optional<ResourceEntity> findByStorageIdAndChecksum(Long storageId, String checksum);
}
