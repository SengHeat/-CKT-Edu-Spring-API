package com.api.content_management.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataStructureParentDTO {
    private Long id;
    private String name;
    private String type;
    private DataStructureParentDTO parent;
}