package com.chachotkin.resource.processor.service;

import com.chachotkin.resource.processor.client.ResourceServiceClient;
import com.chachotkin.resource.processor.client.SongServiceClient;
import com.chachotkin.resource.processor.dto.ResourceMetadata;
import com.chachotkin.resource.processor.dto.SongMetadata;
import com.chachotkin.resource.processor.util.MetadataUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceProcessor {

    private final ResourceServiceClient resourceServiceClient;
    private final SongServiceClient songServiceClient;
    private final ObjectMapper objectMapper;

    private Parser parser;

    @PostConstruct
    void init() {
        parser = new Mp3Parser();
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topic}", groupId = "${kafka.group-id}")
    public void proccess(String message) throws JsonProcessingException {
        log.info("Starting processing received message [{}].", message);

        var resourceMetadata = objectMapper.readValue(message, ResourceMetadata.class);
        var resource = resourceServiceClient.download(resourceMetadata.getId());

        var songMetadata = parseSongMetadata(resourceMetadata.getId(), resource);
        log.info("Successfully extracted song metadata [{}].", songMetadata);

        var songUploadResponse = songServiceClient.upload(songMetadata);
        log.info("Processing finished. Song metadata with id [{}] was saved.", songUploadResponse.getId());
    }

    private SongMetadata parseSongMetadata(Long resourceId, ByteArrayResource resource) {
        var contentHandler = new DefaultHandler();
        var metadata = new Metadata();
        var parseContext = new ParseContext();

        try (var inputStream = resource.getInputStream()) {
            parser.parse(inputStream, contentHandler, metadata, parseContext);
        } catch (TikaException | IOException | SAXException e) {
            log.error("Failed to extract song metadata!", e);
            throw new IllegalStateException(e);
        }

        return SongMetadata.builder()
                .name(MetadataUtils.getName(metadata))
                .artist(MetadataUtils.getArtist(metadata))
                .album(MetadataUtils.getAlbum(metadata))
                .length(MetadataUtils.getLength(metadata))
                .resourceId(resourceId)
                .year(MetadataUtils.getYear(metadata))
                .build();
    }
}
