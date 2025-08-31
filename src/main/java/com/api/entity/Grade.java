package com.api.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "grades")
public class Grade extends BaseEntity {

    @Column(nullable = false)
    private String level;
    private String labelEn;
    private String labelKm;
    private String image;

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
