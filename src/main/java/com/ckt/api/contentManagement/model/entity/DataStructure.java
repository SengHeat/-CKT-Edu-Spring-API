package com.ckt.api.contentManagement.model.entity;


import com.ckt.api.user.model.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "data_structures")
public class DataStructure extends BaseEntity {
    private String name;
    private String description;
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference
    private DataStructure parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DataStructure> children;

    @OneToMany(mappedBy = "dataStructure", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Content> contents;
}
