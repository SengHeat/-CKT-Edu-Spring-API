package com.api.content_management.repository;

import com.api.content_management.model.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContentRepository extends JpaRepository<Content, Long> {

    @Query("SELECT COUNT(component) FROM Component component WHERE component.content.id = :id")
    Long countComponentsById(Long id);
}
