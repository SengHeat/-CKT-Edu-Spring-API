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

    Optional<DataStructure> findFirstByNameAndParentIsNull(String name);
    Optional<DataStructure> findFirstByNameAndParent(String name, DataStructure parent);
    List<DataStructure> findByType(String type);

    @Query("SELECT ds FROM DataStructure ds WHERE ds.type = 'GRADE'")
    List<DataStructure> findAllGrades();

    @Query("SELECT DISTINCT d FROM DataStructure d " +
            "LEFT JOIN FETCH d.parent p " +
            "LEFT JOIN FETCH p.parent pp " +
            "LEFT JOIN FETCH pp.parent " +
            "LEFT JOIN FETCH d.children " +
            "LEFT JOIN FETCH d.contents c " +
            "LEFT JOIN FETCH c.components comp " +
            "LEFT JOIN FETCH comp.componentText " +
            "LEFT JOIN FETCH comp.componentCollapse " +
            "WHERE d.id = :id")
    Optional<DataStructure> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT d FROM DataStructure d " +
            "LEFT JOIN FETCH d.parent p " +
            "LEFT JOIN FETCH p.parent " +
            "LEFT JOIN FETCH d.children c " +
            "LEFT JOIN FETCH c.children " +
            "WHERE d.id = :id")
    Optional<DataStructure> findByIdWithChildrenAndParent(@Param("id") Long id);

}
