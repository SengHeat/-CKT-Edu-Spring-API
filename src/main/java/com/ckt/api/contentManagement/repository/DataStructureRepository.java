package com.ckt.api.contentManagement.repository;

import com.ckt.api.contentManagement.model.entity.DataStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DataStructureRepository extends JpaRepository<DataStructure, Long> {

    // Check if a name exists (existing method)
    boolean existsByName(String name);

    // Check if a name exists under a specific parent
    boolean existsByNameAndParent(String name, DataStructure parent);

    Optional<DataStructure> findByNameAndParentIsNull(String name);
    Optional<DataStructure> findByNameAndParent(String name, DataStructure parent);
    List<DataStructure> findByType(String type);

    @Query("SELECT ds FROM DataStructure ds WHERE ds.type = 'GRADE'")
    List<DataStructure> findAllGrades();

    @Query("SELECT d FROM DataStructure d " +
            "LEFT JOIN FETCH d.children " +
            "LEFT JOIN FETCH d.parent " +
            "WHERE d.id = :id")
    Optional<DataStructure> findByIdWithChildrenAndParent(@Param("id") Long id);


}
