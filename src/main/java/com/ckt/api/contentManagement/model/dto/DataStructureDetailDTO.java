package com.ckt.api.contentManagement.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DataStructureDetailDTO {
    private Long id;
    private String name;
    private String description;
    private String type;
    private Long parentId;
    private DataStructureParentDTO parent;
    private List<DataStructureChildrenDTO> children;
    private ContentDTO content;
}
