package com.ckt.api.contentManagement.mapper;

import com.ckt.api.contentManagement.model.dto.*;
import com.ckt.api.contentManagement.model.entity.DataStructure;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataStructureMapper {

    public static DataStructureDTO toDto(DataStructure entity) {
        if (entity == null) {
            return null;
        }

        return DataStructureDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parent(toWithParentDto(entity.getParent() != null ? entity.getParent() : null))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .updatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy().getId() : null)
                .build();
    }

    public static List<GradeDTO> toGradeDtoList(List<DataStructure> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(entity -> new GradeDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getType()
        )).collect(Collectors.toList());
    }

    public static List<DataStructureDTO> toDtoList(List<DataStructure> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(DataStructureMapper::toDto).collect(Collectors.toList());
    }

    public static DataStructureChildrenDTO toWithChildrenDto(DataStructure entity) {
        if (entity == null) {
            return null;
        }

        return DataStructureChildrenDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .children(entity.getChildren() != null ? toWithChildrenDtoList(entity.getChildren()) : null)
                .build();
    }

    public static List<DataStructureChildrenDTO> toWithChildrenDtoList(List<DataStructure> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(DataStructureMapper::toWithChildrenDto).collect(Collectors.toList());
    }

    public static DataStructureParentDTO toWithParentDto(DataStructure entity) {
        if (entity == null) {
            return null;
        }

        return DataStructureParentDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .parent(entity.getParent() != null ? toWithParentDto(entity.getParent()) : null)
                .build();
    }

    public static List<DataStructureParentDTO> toWithParentDtoList(List<DataStructure> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(DataStructureMapper::toWithParentDto).collect(Collectors.toList());
    }

    public static DataStructureDetailDTO toDetailDto(DataStructure entity) {
        if (entity == null) {
            return null;
        }

        return DataStructureDetailDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parent(toWithParentDto(entity.getParent() != null ? entity.getParent() : null))
                .children(entity.getChildren() != null ? toWithChildrenDtoList(entity.getChildren()) : null)
                .contents(entity.getContents() != null ?
                        entity.getContents().stream()
                                .map(ContentMapper::toDTO)
                                .collect(Collectors.toList())
                        : Collections.emptyList())
                .build();
    }

    public static SubMenuDTO toSubMenuDto(DataStructure entity) {
        if (entity == null) {
            return null;
        }

        return SubMenuDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parent(toWithParentDto(entity.getParent() != null ? entity.getParent() : null))
                .children(entity.getChildren() != null ? toWithChildrenDtoList(entity.getChildren()) : null)
                .build();
    }

    public static DataStructure toEntity(DataStructureDTO dto) {
        if (dto == null) {
            return null;
        }

        DataStructure entity = new DataStructure();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        return entity;
    }
}
