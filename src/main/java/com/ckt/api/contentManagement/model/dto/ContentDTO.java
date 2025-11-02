package com.ckt.api.contentManagement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDTO {
    private Long id;
    private Long dataStructureId;
    private String title;
    private String description;
    private List<ComponentDTO> components;
}
