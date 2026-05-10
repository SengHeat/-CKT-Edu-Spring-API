package com.ckt.api.contentManagement.model.entity;


import com.ckt.api.user.model.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<DataStructure> children = new HashSet<>();  // ← was List

    @OneToMany(mappedBy = "dataStructure", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Content> contents = new HashSet<>();        // ← was List
}
