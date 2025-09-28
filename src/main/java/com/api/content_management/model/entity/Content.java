package com.api.content_management.model.entity;

import com.api.user.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "contents")
public class Content extends BaseEntity {
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "data_structure_id", referencedColumnName = "id")
    private DataStructure dataStructure;

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Component> components;
}
