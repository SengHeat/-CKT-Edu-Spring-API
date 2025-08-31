package com.api.repository;

import com.api.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByGroup(String group);

    @Query("SELECT p FROM Permission p WHERE p.id IN :ids")
    List<Permission> findPermissionsByIds(@Param("ids") List<Long> ids);

}
