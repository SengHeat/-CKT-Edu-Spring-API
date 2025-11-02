package com.ckt.api.contentManagement.controller;

import com.ckt.api.base.ApiResponse;
import com.ckt.api.contentManagement.model.dto.ComponentCollapseRequest;
import com.ckt.api.contentManagement.model.dto.ComponentCollapseResponse;
import com.ckt.api.contentManagement.service.ComponentCollapseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/component-collapses")
@RequiredArgsConstructor
public class ComponentCollapseController {

    private final ComponentCollapseService componentCollapseService;

    @PostMapping
    public ResponseEntity<Void> create(
            @Valid @RequestBody ComponentCollapseRequest request) {

            componentCollapseService.create(request);

            return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComponentCollapseResponse>>> getAll() {
        List<ComponentCollapseResponse> responses = componentCollapseService.findAll();
        return ResponseEntity.ok(ApiResponse.success(responses, "Component collapses retrieved successfully"));
    }
}