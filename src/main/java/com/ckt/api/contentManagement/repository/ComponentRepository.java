package com.ckt.api.contentManagement.repository;

import com.ckt.api.contentManagement.model.entity.Component;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComponentRepository extends JpaRepository<Component, Long> {
}
