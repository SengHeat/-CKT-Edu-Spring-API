package com.api.content_management.model.entity;


import com.api.user.model.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

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

    @OneToOne(mappedBy = "dataStructure")
    private Content content;


    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public void setType(String type) { this.type = type; }
    public String getType() { return type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DataStructure getParent() { return parent; }
    public void setParent(DataStructure parent) { this.parent = parent; }

    public List<DataStructure> getChildren() { return children; }
    public void setChildren(List<DataStructure> children) { this.children = children; }

    public Content getContent() { return content; }
    public void setContent(Content content) { this.content = content; }
}
