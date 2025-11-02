package com.ckt.api.user.controller;

import com.ckt.api.base.PaginatedResponse;
import com.ckt.api.user.model.entity.User;
import com.ckt.api.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
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

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public User show(@PathVariable Long id) {
        return users.find(id);
    }

}
