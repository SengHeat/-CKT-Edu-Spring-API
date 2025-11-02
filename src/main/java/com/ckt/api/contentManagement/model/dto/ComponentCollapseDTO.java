package com.ckt.api.contentManagement.model.dto;

import com.ckt.api.enums.ComponentTextType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentCollapseDTO {
    private Long id;
    private Long mainComponentId;
    private String dataType = ComponentTextType.COLLAPSE.name();
    private String data;
}
