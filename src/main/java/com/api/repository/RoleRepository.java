package com.api.repository;

import com.api.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByGroup(String group);
    Optional<Role> findByName(String role);
}
