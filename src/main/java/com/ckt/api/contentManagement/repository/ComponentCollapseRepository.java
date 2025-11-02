package com.ckt.api.contentManagement.repository;

import com.ckt.api.contentManagement.model.entity.ComponentCollapse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComponentCollapseRepository extends JpaRepository<ComponentCollapse, Integer> {
    
    List<ComponentCollapse> findByComponentId(Integer componentId);
}