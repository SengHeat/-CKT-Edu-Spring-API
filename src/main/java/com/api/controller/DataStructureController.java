package com.api.controller;

import com.api.dto.DataStructureDto;
import com.api.dto.DataStructureWithChildrenDto;
import com.api.dto.StoreDataStructureRequest;
import com.api.service.DataStructureService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/data-structures")
public class DataStructureController {

    private final DataStructureService dataStructureService;

    public DataStructureController(DataStructureService dataStructureService) {
        this.dataStructureService = dataStructureService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DELETE_DATA_STRUCTURE')")
    public ResponseEntity<Iterable<DataStructureDto>> index(@RequestParam(required = false) String type) {
        return ResponseEntity.ok(dataStructureService.findAll(Optional.ofNullable(type)));
    }

    @GetMapping("/grades")
    public ResponseEntity<Iterable<DataStructureDto>> grades() {
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
    public ResponseEntity<DataStructureWithChildrenDto> show(@PathVariable Long id) {
        return ResponseEntity.ok(dataStructureService.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_DATA_STRUCTURE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dataStructureService.delete(id);
        return ResponseEntity.status(204).build();
    }
}
