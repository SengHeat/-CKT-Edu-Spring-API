package com.ckt.api.contentManagement.mapper;

import com.ckt.api.contentManagement.model.dto.ComponentCollapseDTO;
import com.ckt.api.contentManagement.model.dto.ComponentDTO;
import com.ckt.api.contentManagement.model.dto.ComponentMediaDTO;
import com.ckt.api.contentManagement.model.dto.ComponentTextDTO;
import com.ckt.api.contentManagement.model.entity.Component;
import com.ckt.api.contentManagement.model.entity.ComponentCollapse;
import com.ckt.api.contentManagement.model.entity.ComponentMedia;
import com.ckt.api.contentManagement.model.entity.ComponentText;
import com.ckt.api.enums.ComponentTextType;

import java.util.List;
import java.util.stream.Collectors;

public class ComponentMapper {

    public static ComponentDTO toDto(Component components) {
        if (components == null) {
            return null;
        }

        Object object = null;

        if(components.getComponentText() != null) {
            object = components.getComponentText();
        }

        if(components.getComponentCollapse() != null) {
            object = components.getComponentCollapse();
        }

        return ComponentDTO.builder()
                .id(components.getId())
                .contentId(components.getContent().getId())
                .position(components.getPosition())
                .componentData(componentData(object))
                .build();
    }

    public static List<ComponentDTO> toDtoList(List<Component> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(ComponentMapper::toDto).collect(Collectors.toList());
    }


    public static Object componentData(Object data) {

        if(data == null) {
            return null;
        }

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
            return ComponentCollapseDTO.builder()
                    .id(((ComponentCollapse) data).getId())
                    .mainComponentId(((ComponentCollapse) data).getComponent().getId())
                    .dataType(ComponentTextType.COLLAPSE.name())
                    .data(((ComponentCollapse) data).getData())
                    .build();
        }

        return null;
    }
}
