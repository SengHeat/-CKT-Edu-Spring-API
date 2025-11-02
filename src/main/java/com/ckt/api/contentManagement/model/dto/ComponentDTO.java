package com.ckt.api.contentManagement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentDTO {
    private Long id;
    private Long contentId;
    private Long position;
    private Object componentData;
}