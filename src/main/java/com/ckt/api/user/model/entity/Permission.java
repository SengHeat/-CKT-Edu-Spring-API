package com.ckt.api.user.model.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {
    @Column(name = "\"group\"",unique = false, nullable = false, length = 150)
    private String group;
    private String name;

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        return Objects.equals(getGroup(), ((Permission) o).getGroup());
    }
    @Override public int hashCode() { return Objects.hash(getGroup()); }
}
