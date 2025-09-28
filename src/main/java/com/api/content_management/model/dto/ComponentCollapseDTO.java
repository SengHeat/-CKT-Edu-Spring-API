package com.api.content_management.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentCollapseDTO {
    private Integer id;
    private Integer mainComponentId;
    private Integer displayContentId;
    private Integer collapseContentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
    private ComponentDTO mainComponent; // Relationship to Component
    private ContentDTO displayContent; // Relationship to Content for display
    private ContentDTO collapseContent; // Relationship to Content for collapsed section
}
