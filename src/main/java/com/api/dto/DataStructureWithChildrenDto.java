package com.api.dto;

import com.api.entity.DataStructure;

import java.util.List;

public record DataStructureWithChildrenDto(
    Long id, String name, String description, String type,
    DataStructureDto parent,
    List<DataStructureDto> children
) {}