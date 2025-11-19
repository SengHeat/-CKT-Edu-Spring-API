package com.ckt.api.user.controller;

import com.ckt.api.base.ApiResponse;
import com.ckt.api.base.PaginatedResponse;
import com.ckt.api.user.model.entity.User;
import com.ckt.api.user.service.AuthService;
import com.ckt.api.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService users;
    private final AuthService authService;

    public UserController(UserService users, AuthService authService) {
        this.users = users;
        this.authService = authService;
    }

    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<User>> searchUsers(
            @RequestParam String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int perPage,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {

        PaginatedResponse<User> response = users.searchUsers(name, page, perPage, sortBy, order);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<User> index(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size) {
        return users.list(page, size);
    }

    @GetMapping("/admin/profile")
    public ResponseEntity<ApiResponse<?>> adminProfile() {
        return ResponseEntity.ok(ApiResponse.success(authService.getAdminProfile()));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> profile(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(ApiResponse.success(authService.getUserProfile()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> show(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(users.find(id)));
    }

}
