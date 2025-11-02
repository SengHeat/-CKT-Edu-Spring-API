package com.ckt.api.contentManagement.model.entity;


import com.ckt.api.user.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "components")
public class Component extends BaseEntity {
    private Long position;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "content_id", referencedColumnName = "id")
    private Content content;

    @OneToOne(mappedBy = "component")
    private ComponentText componentText;

    @OneToOne(mappedBy = "component")
    private ComponentCollapse componentCollapse;
}
