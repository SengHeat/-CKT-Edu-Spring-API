package com.ckt.api.user.service;


import com.ckt.api.user.model.entity.User;
import com.ckt.api.user.repository.PermissionRepository;
import com.ckt.api.user.repository.RoleRepository;
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

    public static User getAuthenticatedUser() {
        // Placeholder for actual authentication logic
        return null;
    }
}
