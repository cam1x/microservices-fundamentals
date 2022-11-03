package com.chachotkin.resource.service.cucumber;

import com.chachotkin.resource.service.cucumber.definition.ResourceMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.DataTableType;
import io.cucumber.java.DefaultDataTableCellTransformer;
import io.cucumber.java.DefaultDataTableEntryTransformer;
import io.cucumber.java.DefaultParameterTransformer;

import java.lang.reflect.Type;
import java.util.Map;

public class ParameterTypes {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @DefaultParameterTransformer
    @DefaultDataTableEntryTransformer
    @DefaultDataTableCellTransformer
    public Object transformer(Object fromValue, Type toValueType) {
        return objectMapper.convertValue(fromValue, objectMapper.constructType(toValueType));
    }

    @DataTableType
    public ResourceMetadata resourceMetadataEntry(Map<String, String> entry) {
        var resourceMetadataBuilder = ResourceMetadata.builder()
                .id(Long.valueOf(entry.get("id")))
                .fileName(entry.get("fileName"));

        var size = entry.get("size");
        if (size != null) {
            resourceMetadataBuilder.size(Long.valueOf(size));
        }

        return resourceMetadataBuilder
                .build();
    }
}
