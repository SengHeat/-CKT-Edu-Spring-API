package com.api.service;


import com.api.entity.User;
import com.api.repository.PermissionRepository;
import com.api.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RBACService {
    private final RoleRepository roleRepo;
    private final PermissionRepository permRepo;

    public RBACService(RoleRepository roleRepo, PermissionRepository permRepo) {
        this.roleRepo = roleRepo;
        this.permRepo = permRepo;
    }

    public boolean userHasRole(User u, String roleSlug) {
        return u.getRoles().stream().anyMatch(r -> r.getGroup().equals(roleSlug));
    }

    public boolean userHasPermission(User u, String permSlug) {
        return u.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .anyMatch(p -> p.getName().equals(permSlug));
    }
}
