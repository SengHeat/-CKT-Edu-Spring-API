package com.api.content_management.controller;

import com.api.content_management.model.dto.DataStructureDTO;
import com.api.content_management.model.dto.DataStructureDetailDTO;
import com.api.content_management.model.dto.StoreDataStructureRequest;
import com.api.content_management.service.DataStructureService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/data-structures")
public class DataStructureController {

    private final DataStructureService dataStructureService;

    public DataStructureController(DataStructureService dataStructureService) {
        this.dataStructureService = dataStructureService;
    }

    @GetMapping
    public ResponseEntity<List<DataStructureDTO>> index(@RequestParam(required = false) String type) {
        return ResponseEntity.ok(dataStructureService.findAll(Optional.ofNullable(type)));
    }

    @GetMapping("/grades")
    public ResponseEntity<Iterable<DataStructureDTO>> grades() {
        return ResponseEntity.ok(dataStructureService.findAllGrades());
    }


    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_DATA_STRUCTURE')")
    public ResponseEntity<Void> create(@Valid @RequestBody StoreDataStructureRequest request) {
        dataStructureService.create(request);
        return ResponseEntity.status(204).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_DATA_STRUCTURE')")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody StoreDataStructureRequest request) {
        dataStructureService.update(id, request);
        return ResponseEntity.status(204).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataStructureDetailDTO> show(@PathVariable Long id) {
        return ResponseEntity.ok(dataStructureService.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_DATA_STRUCTURE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dataStructureService.delete(id);
        return ResponseEntity.status(204).build();
    }
}
