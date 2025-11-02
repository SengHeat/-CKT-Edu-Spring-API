package com.ckt.api.contentManagement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentMediaDTO {
    private Integer id;
    private Integer mainComponentId;
    private String dataType;
    private String fileName;
    private String filePath;
    private String privateUrl;
    private String publicUrl;
    private String metadata; // Representing JSON as String for simplicity in DTO
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
    private ComponentDTO mainComponent; // Relationship to Component
}


