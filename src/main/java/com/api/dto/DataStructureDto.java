package com.api.dto;

import com.api.entity.DataStructure;

import java.util.List;

public record DataStructureDto(
        Long id,
        String name,
        String description,
        String type,
        DataStructureDto parent
) {}