package com.api.content_management.model.dto;

import com.api.enums.ComponentTextType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
public class StoreComponentTextRequest {

    @NotNull
    private ComponentTextType dataType;

    @Getter
    @NotNull
    private String data;

    public String getDataType() {
        return dataType.name();
    }

}
