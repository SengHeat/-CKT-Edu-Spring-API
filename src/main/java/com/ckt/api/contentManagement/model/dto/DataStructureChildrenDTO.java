package com.ckt.api.contentManagement.model.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DataStructureChildrenDTO {
    private Long id;
    private String name;
    private String type;
    private Long parentId;
    private String description;
    private List<DataStructureChildrenDTO> children;
}