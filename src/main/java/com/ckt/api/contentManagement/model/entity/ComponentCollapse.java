package com.ckt.api.contentManagement.model.entity;

import com.ckt.api.user.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "component_collapses")
public class ComponentCollapse extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    private String data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", referencedColumnName = "id")
    private Component component;
}