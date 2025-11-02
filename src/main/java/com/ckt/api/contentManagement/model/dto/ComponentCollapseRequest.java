package com.ckt.api.contentManagement.model.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentCollapseRequest {
    
    @NotNull(message = "Content ID is required")
    private Long contentId;
    
    @NotNull(message = "Collapse data is required")
    private String data;
}