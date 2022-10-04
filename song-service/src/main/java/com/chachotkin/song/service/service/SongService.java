package com.chachotkin.song.service.service;

import com.chachotkin.song.service.dto.DeleteResponseDto;
import com.chachotkin.song.service.dto.SongDto;
import com.chachotkin.song.service.dto.UploadResponseDto;
import com.chachotkin.song.service.entity.SongEntity;
import com.chachotkin.song.service.exception.ResourceNotFoundException;
import com.chachotkin.song.service.mapper.SongMapper;
import com.chachotkin.song.service.repository.SongRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final SongMapper songMapper;

    public UploadResponseDto upload(@NonNull SongDto songDto) {
        var songEntity = songMapper.toEntity(songDto);
        var savedSong = songRepository.save(songEntity);
        return new UploadResponseDto(savedSong.getId());
    }

    public SongDto retrieve(@NonNull Long id) {
        return songRepository.findById(id)
                .map(songMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public DeleteResponseDto delete(@NonNull Collection<Long> ids) {
        var songs = songRepository.findAllById(ids);

        if (songs.isEmpty()) {
            throw new ResourceNotFoundException(String.format("No songs found for provided ids [%s]", ids));
        }

        var idsToDelete = songs.stream()
                .map(SongEntity::getId)
                .toList();
        songRepository.deleteAllById(idsToDelete);
        return new DeleteResponseDto(idsToDelete);
    }
}
