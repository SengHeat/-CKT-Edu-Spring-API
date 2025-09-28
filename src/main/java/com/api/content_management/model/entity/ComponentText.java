package com.api.content_management.model.entity;


import com.api.user.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "component_texts")
public class ComponentText extends BaseEntity {

    private String dataType;

    @Column(columnDefinition = "TEXT")
    private String data;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "component_id", referencedColumnName = "id")
    private Component component;
}
