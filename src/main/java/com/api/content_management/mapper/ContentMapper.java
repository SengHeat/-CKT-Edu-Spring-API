package com.api.content_management.mapper;

import com.api.content_management.model.dto.ContentDTO;
import com.api.content_management.model.entity.Content;

public class ContentMapper {
    public static ContentDTO toDTO(Content content) {
        if (content == null) {
            return null;
        }
        return ContentDTO.builder()
                .id(content.getId())
                .title(content.getTitle())
                .description(content.getDescription())
                .components(ComponentMapper.toDtoList(content.getComponents()))
                .build();
    }
}
