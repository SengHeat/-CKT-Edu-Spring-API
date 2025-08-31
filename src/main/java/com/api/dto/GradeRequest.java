package com.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class GradeRequest {

    @NotBlank
    private String level;
    @NotBlank
    private String labelEn;
    @NotBlank
    private String labelKm;

    private String image;

    public GradeRequest(String level, String labelEn, String labelKm, String image) {
        this.level = level;
        this.labelEn = labelEn;
        this.labelKm = labelKm;
        this.image = image;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
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
