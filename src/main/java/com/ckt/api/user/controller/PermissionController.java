package com.ckt.api.user.controller;




import com.ckt.api.user.model.entity.Permission;
import com.ckt.api.user.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/permissions")
@RestController
public class PermissionController {

    public final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity<List<Permission>> findAll() {
        List<Permission> permissions = permissionService.findAll();

        for (Permission p : permissions) {
            if (p.getName() != null) {
                p.setName(p.getName().replace("_", " "));
            }
        }

        return ResponseEntity.ok(permissions);
    }
}
