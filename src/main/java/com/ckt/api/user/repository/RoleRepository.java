package com.ckt.api.user.repository;

import com.ckt.api.user.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findFirstByGroup(String group);
    Optional<Role> findFirstByName(String role);
}
