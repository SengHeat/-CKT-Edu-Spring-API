package com.api.content_management.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Objects;

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