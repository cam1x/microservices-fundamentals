package com.chachotkin.resource.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@Getter
@AllArgsConstructor
public class DeleteResponseDto {

    private final Collection<Long> ids;
}
