package com.api.controller;


import com.api.dto.PermissionAssignRequest;
import com.api.entity.Role;
import com.api.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/roles")
@RestController
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }


    @GetMapping("")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @PostMapping("/assign/role/{roleId}/permissions")
    public ResponseEntity<Void> assignPermissionRole(
            @PathVariable Long roleId,
            @RequestBody PermissionAssignRequest request) {

        if (request.getPermissionIds() == null || request.getPermissionIds().isEmpty()) {
            throw new IllegalArgumentException("Permission IDs cannot be empty");
        }

        roleService.assignMultiplePermissionRole(roleId, request.getPermissionIds());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign/user/{userId}/role/{roleId}")
    public ResponseEntity<Void> assignUserRole(
            @PathVariable Long userId,
            @PathVariable Long roleId) {

        roleService.assignUserRole(userId, roleId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/remove/user/{userId}/role/{roleId}")
    public ResponseEntity<Void> removeUserRole(
            @PathVariable Long userId,
            @PathVariable Long roleId) {

        roleService.removeUserRole(userId, roleId);
        return ResponseEntity.noContent().build();
    }
}
