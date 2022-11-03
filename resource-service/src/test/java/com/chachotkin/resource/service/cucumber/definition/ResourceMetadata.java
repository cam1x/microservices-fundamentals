package com.chachotkin.resource.service.cucumber.definition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceMetadata {

    private Long id;
    private String fileName;
    private Long size;
}
