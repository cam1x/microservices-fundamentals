package com.chachotkin.song.service.mapper;

import com.chachotkin.song.service.dto.SongDto;
import com.chachotkin.song.service.entity.SongEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SongMapper {

    SongDto toDto(SongEntity songEntity);

    SongEntity toEntity(SongDto songDto);
}
