package com.ckt.api.contentManagement.model.dto;

import com.ckt.api.enums.ComponentTextType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentCollapseDTO {
    private Long id;
    private Long mainComponentId;
    private String dataType;
    private String data;
}
