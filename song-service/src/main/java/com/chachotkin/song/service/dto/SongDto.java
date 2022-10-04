package com.chachotkin.song.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongDto {

    // example: 2:59, 1:21:02
    private static final String TIMESTAMP_REGEX = "^(?:\\d+(?::[0-5][0-9]:[0-5][0-9])?|[0-5]?[0-9]:[0-5][0-9])$";

    @NotBlank
    private String name;

    @NotBlank
    private String artist;

    private String album;

    @NotNull
    @Pattern(
            regexp = TIMESTAMP_REGEX,
            message = "Song length should have valid time format."
    )
    private String length;

    @NotNull
    @Positive
    private Long resourceId;

    @NotNull
    @Range(min = 1800, max = 2022, message = "Song should be released between 1800 and 2022.")
    private Integer year;
}
