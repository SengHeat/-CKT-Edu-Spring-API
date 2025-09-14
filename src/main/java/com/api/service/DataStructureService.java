package com.api.service;


import com.api.dto.DataStructureDto;
import com.api.dto.DataStructureWithChildrenDto;
import com.api.dto.StoreDataStructureRequest;
import com.api.entity.DataStructure;
import com.api.exception.DuplicateException;
import com.api.exception.NotFoundException;
import com.api.repository.DataStructureRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DataStructureService {

    private final DataStructureRepository dataStructureRepository;

    public DataStructureService(DataStructureRepository dataStructureRepository) {
        this.dataStructureRepository = dataStructureRepository;
    }


    @Transactional
    public void create(StoreDataStructureRequest request) {

        DataStructure parent = null;
        if(request.getParentId() != null) {
            parent = dataStructureRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent not found"));
        }

        // Check for duplicate under parent
        if (dataStructureRepository.existsByNameAndParent(request.getName(), parent)) {
            throw new DuplicateException("Data structure with this name already exists under the specified parent.");
        }
        DataStructure entity = new DataStructure();
        entity.setName(request.getName());
        entity.setType(request.getType());
        entity.setDescription(request.getDescription());
        entity.setParent(parent);

        dataStructureRepository.save(entity);
    }

    public void update(Long id, StoreDataStructureRequest request) {
        DataStructure dataStructure = findEntityById(id);

        if (dataStructure != null) {
            dataStructure.setName(request.getName());
            dataStructure.setDescription(request.getDescription());

            if(request.getParentId() != null) {
                dataStructure.setParent(findEntityById(request.getParentId()));
            } else {
                dataStructure.setParent(null);
            }

            dataStructureRepository.save(dataStructure);
        }
    }

    public DataStructureWithChildrenDto findById(Long id) {
        return dataStructureRepository.findById(id)
                .map(this::toWithChildrenDto)
                .orElseThrow(() -> new NotFoundException("Data structure not found"));
    }

    public List<DataStructureDto> findAll(Optional<String> typeOpt) {
        List<DataStructure> roots;

        if (typeOpt.isPresent()) {
            roots = dataStructureRepository.findByType(typeOpt.get());
        } else {
            roots = dataStructureRepository.findAll();
        }
        return roots.stream().map(this::toDto).toList();
    }

    public List<DataStructureDto> findAllGrades() {
        List<DataStructure> grades = dataStructureRepository.findAllGrades();
        return grades.stream().map(this::toDto).toList();
    }

    public void delete(Long id) {
        DataStructure dataStructure = findEntityById(id);
        if (dataStructure != null) {
            dataStructureRepository.delete(dataStructure);
        }
    }

    public DataStructure findEntityById(Long id) {
        return dataStructureRepository.findById(id).orElse(null);
    }


    private DataStructureDto toDto(DataStructure entity) {
        return new DataStructureDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getType(),
                entity.getParent() == null ? null : toDto(entity.getParent()
            )
        );
    }


    private DataStructureWithChildrenDto toWithChildrenDto(DataStructure entity) {
        return new DataStructureWithChildrenDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getType(),
                entity.getParent() == null ? null : toDto(entity.getParent()), // parent = simple DTO
                entity.getChildren() == null || entity.getChildren().isEmpty()
                        ? List.of()
                        : entity.getChildren().stream()
                        .map(this::toDto)
                        .toList()
        );
    }



}
