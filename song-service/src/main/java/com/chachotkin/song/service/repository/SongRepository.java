package com.chachotkin.song.service.repository;

import com.chachotkin.song.service.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<SongEntity, Long> {
}
