package com.ckt.api.contentManagement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentTextDTO {
    private Long id;
    private Long mainComponentId;
    private String dataType;
    private String data;
}


