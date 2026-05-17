package com.ckt.api.contentManagement.repository;

import com.ckt.api.contentManagement.model.entity.Content;
import com.ckt.api.contentManagement.model.entity.DataStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {

    @Query("SELECT COUNT(component) FROM Component component WHERE component.content.id = :id")
    Long countComponentsById(Long id);

    Optional<Content> findFirstByDataStructure(DataStructure dataStructure);
}
