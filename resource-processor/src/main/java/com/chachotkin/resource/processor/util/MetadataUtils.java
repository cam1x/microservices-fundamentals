package com.chachotkin.resource.processor.util;

import io.micrometer.core.instrument.util.StringUtils;
import org.apache.tika.metadata.Metadata;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MetadataUtils {

    private static final String DATE_FORMAT = "HH:mm:ss";

    private static final String TITLE_METADATA = "dc:title";
    private static final String ARTIST_METADATA = "xmpDM:artist";
    private static final String ALBUM_METADATA = "xmpDM:album";
    private static final String DURATION_METADATA = "xmpDM:duration";
    private static final String RELEASE_DATE_METADATA = "xmpDM:releaseDate";

    public static String getName(Metadata metadata) {
        return metadata.get(TITLE_METADATA);
    }

    public static String getArtist(Metadata metadata) {
        return metadata.get(ARTIST_METADATA);
    }

    public static String getAlbum(Metadata metadata) {
        return metadata.get(ALBUM_METADATA);
    }

    public static String getLength(Metadata metadata) {
        var duration = metadata.get(DURATION_METADATA);
        var doubleDuration = Double.parseDouble(duration);
        var durationInMillis = (long) (doubleDuration * 1000);
        return LocalTime.MIDNIGHT
                .plus(Duration.ofMillis(durationInMillis))
                .format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    public static Integer getYear(Metadata metadata) {
        var releaseDate = metadata.get(RELEASE_DATE_METADATA);
        if (StringUtils.isBlank(releaseDate)) {
            return null;
        }

        return Integer.valueOf(releaseDate);
    }
}
