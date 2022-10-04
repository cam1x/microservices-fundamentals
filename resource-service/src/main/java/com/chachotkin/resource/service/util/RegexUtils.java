package com.chachotkin.resource.service.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegexUtils {

    // example: s3://resources/audio/file_example_MP3_2MG.mp3
    public static final Pattern S3_SOURCE_PATH = Pattern.compile("^s3:\\/\\/(\\w[\\w|\\.|\\~]{1,61}\\w)\\/(.+)$");

    // example: bytes=0-100, bytes=100-, bytes=k-n
    public static final Pattern RANGE_VALUE = Pattern.compile("^bytes=(\\d+)\\-(\\d*)$");
}
