package com.ckt.api.contentManagement.model.entity;

import com.ckt.api.user.model.entity.BaseEntity;
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

    @ManyToOne
    @JoinColumn(name = "data_structure_id") // This creates the Foreign Key in the Content table
    private DataStructure dataStructure;

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Component> components;
}
