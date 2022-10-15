package com.chachotkin.resource.processor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SongMetadata {

    @NonNull
    private String name;

    @NonNull
    private String artist;

    private String album;

    @NonNull
    private String length;

    @NonNull
    private Long resourceId;

    private Integer year;
}
