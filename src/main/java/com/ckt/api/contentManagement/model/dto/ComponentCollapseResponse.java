package com.ckt.api.contentManagement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentCollapseResponse {
    
    private Long id;
    private Long componentId;
    private String data;
}