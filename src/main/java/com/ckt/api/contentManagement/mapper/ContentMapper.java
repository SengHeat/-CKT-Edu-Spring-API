package com.ckt.api.contentManagement.mapper;

import com.ckt.api.contentManagement.model.dto.ContentDTO;
import com.ckt.api.contentManagement.model.entity.Content;

public class ContentMapper {
    public static ContentDTO toDTO(Content content) {
        if (content == null) {
            return null;
        }
        return ContentDTO.builder()
                .id(content.getId())
                .dataStructureId(content.getDataStructure() != null ? content.getDataStructure().getId() : null)
                .title(content.getTitle())
                .description(content.getDescription())
                .components(ComponentMapper.toDtoList(content.getComponents()))
                .build();
    }
}
