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
public class ComponentTextDTO {
    private Long id;
    private Long mainComponentId;
    private String dataType;
    private String data;
}


