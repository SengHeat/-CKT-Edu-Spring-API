package com.api.content_management.service;


import com.api.content_management.mapper.DataStructureMapper;
import com.api.content_management.model.dto.DataStructureDTO;
import com.api.content_management.model.dto.DataStructureDetailDTO;
import com.api.content_management.model.dto.StoreDataStructureRequest;
import com.api.content_management.model.entity.DataStructure;
import com.api.exception.DuplicateException;
import com.api.exception.NotFoundException;
import com.api.content_management.repository.DataStructureRepository;
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

    public DataStructureDetailDTO findById(Long id) {
        DataStructure dataStructure = dataStructureRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Data structure not found"));

        return DataStructureMapper.toDetailDto(dataStructure);
    }

    public List<DataStructureDTO> findAll(Optional<String> typeOpt) {
        List<DataStructure> dataStructures;

        if (typeOpt.isPresent()) {
            dataStructures = dataStructureRepository.findByType(typeOpt.get());
        } else {
            dataStructures = dataStructureRepository.findAll();
        }
        return DataStructureMapper.toDtoList(dataStructures.stream().toList());
    }

    public List<DataStructureDTO> findAllGrades() {
        List<DataStructure> dataStructures = dataStructureRepository.findAllGrades();
        return DataStructureMapper.toDtoList(dataStructures);
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
}
