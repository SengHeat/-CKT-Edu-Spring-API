package com.api.entity;

import com.api.config.JsonMapConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "subjects")
public class Subject extends BaseEntity {
    @Column(nullable = false)
    private String labelEn;
    private String labelKm;
    private String image;


    @Column(columnDefinition = "TEXT")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> meta = new HashMap<>();


    @JsonProperty
    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }
    public String getLabelEn() {
        return labelEn;
    }

    public void setLabelEn(String labelEn) {
        this.labelEn = labelEn;
    }

    public String getLabelKm() {
        return labelKm;
    }

    public void setLabelKm(String labelKm) {
        this.labelKm = labelKm;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
