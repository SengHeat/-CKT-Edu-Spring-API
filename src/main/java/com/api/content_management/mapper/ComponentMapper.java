package com.api.content_management.mapper;

import com.api.content_management.model.dto.ComponentCollapseDTO;
import com.api.content_management.model.dto.ComponentDTO;
import com.api.content_management.model.dto.ComponentMediaDTO;
import com.api.content_management.model.dto.ComponentTextDTO;
import com.api.content_management.model.entity.Component;
import com.api.content_management.model.entity.ComponentCollapse;
import com.api.content_management.model.entity.ComponentMedia;
import com.api.content_management.model.entity.ComponentText;

import java.util.List;
import java.util.stream.Collectors;

public class ComponentMapper {

    public static ComponentDTO toDto(Component components) {
        if (components == null) {
            return null;
        }
        return ComponentDTO.builder()
                .id(components.getId())
                .contentId(components.getContent().getId())
                .position(components.getPosition())
                .componentData(componentData(components.getComponentText()))
                .build();
    }

    public static List<ComponentDTO> toDtoList(List<Component> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(ComponentMapper::toDto).collect(Collectors.toList());
    }


    public static Object componentData(Object data) {

        if(data.getClass() == ComponentText.class) {
            return ComponentTextDTO.builder()
                    .id(((ComponentText) data).getId())
                    .mainComponentId(((ComponentText) data).getComponent().getId())
                    .dataType(((ComponentText) data).getDataType())
                    .data(((ComponentText) data).getData())
                    .build();
        }

        if(data.getClass() == ComponentMedia.class) {
            return ComponentMediaDTO.builder()
//                    .id(((ComponentMedia) data).getId())
//                    .mainComponentId(((ComponentMedia) data).getComponent().getId())
//                    .mediaType(((ComponentMedia) data).getMediaType())
//                    .url(((ComponentMedia) data).getUrl())
                    .build();
        }

        if(data.getClass() == ComponentCollapse.class) {
            return ComponentCollapseDTO.builder();
        }

        return null;
    }
}
