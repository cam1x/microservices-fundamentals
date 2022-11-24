package com.chachotkin.storage.service.repository;

import com.chachotkin.storage.service.entity.StorageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageRepository extends JpaRepository<StorageEntity, Long> {
}
