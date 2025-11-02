package com.ckt.api.contentManagement.model.dto;

import lombok.*;

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