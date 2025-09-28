package com.api.content_management.model.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StoreDataStructureRequest {
    private String name;
    private String description;
    private String type;
    private Long parentId;
}
