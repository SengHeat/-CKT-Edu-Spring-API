package com.ckt.api.user.service;


import com.ckt.api.user.model.entity.Permission;
import com.ckt.api.user.model.entity.Role;
import com.ckt.api.user.model.entity.User;
import com.ckt.api.user.repository.PermissionRepository;
import com.ckt.api.user.repository.RoleRepository;
import com.ckt.api.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;


    public RoleService(RoleRepository roleRepository, UserRepository userRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<Role> findAll() {

        return roleRepository.findAll();
    }

    public void assignUserRole(Long roleId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Assign user role fails, User is not found."));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Assign user role fails, Role is not found."));

        user.getRoles().add(role);
        userRepository.save(user);
    }


    @Transactional
    public void assignMultiplePermissionRole(Long roleId, List<Long> permissionsIds) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Assign user role fails, Role is not found."));

        List<Permission> permissions = permissionRepository.findPermissionsByIds(permissionsIds);

        if (permissionsIds == null) {
            throw new IllegalArgumentException("Update fails, ids cannot be empty");
        }

        if(permissions.isEmpty()) {
            throw new IllegalArgumentException("Update fails, permission cannot be empty");
        }

        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);
        roleRepository.save(role);
    }


    public void removeUserRole(Long roleId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Assign user role fails, User is not found."));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Assign user role fails, Role is not found."));

        user.getRoles().remove(role);
        userRepository.save(user);
    }
}
