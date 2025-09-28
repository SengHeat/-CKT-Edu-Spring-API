package com.api.content_management.controller;

import com.api.content_management.model.dto.StoreComponentTextRequest;
import com.api.content_management.service.ComponentTextService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/component-texts")
public class ComponentTextController {

    private final ComponentTextService componentTextService;

    public ComponentTextController(ComponentTextService componentTextService) {
        this.componentTextService = componentTextService;
    }


    @PostMapping("/{contentId}")
    @PreAuthorize("hasAuthority('CREATE_COMPONENT_TEXT')")
    public ResponseEntity<Void> create(
            @Valid @RequestBody StoreComponentTextRequest request,
            @PathVariable("contentId") Long contentId) {
        componentTextService.createComponentText(request, contentId);

        return ResponseEntity.noContent().build();
    }
}
