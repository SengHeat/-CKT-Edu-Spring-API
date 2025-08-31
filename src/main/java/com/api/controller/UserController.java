package com.api.controller;

import com.api.entity.User;
import com.api.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
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
