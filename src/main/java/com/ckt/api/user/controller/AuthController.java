package com.ckt.api.user.controller;

import com.ckt.api.base.ApiResponse;
import com.ckt.api.user.dto.LoginRequest;
import com.ckt.api.user.dto.RegisterRequest;
import com.ckt.api.user.model.entity.User;
import com.ckt.api.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final PasswordEncoder encoder;

    public AuthController(AuthService auth, PasswordEncoder encoder) {
        this.auth = auth;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        User u = auth.register(req);
        String patToken = auth.createPAT(u.getId(), "default", "*");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of("user", u,"accessToken", patToken)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        User u = auth.findByEmail(req.getEmail()).orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!encoder.matches(req.getPassword(), u.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
        String patToken = auth.createPAT(u.getId(), "default", "*");
        return ResponseEntity.ok( ApiResponse.success(Map.of("accessToken", patToken)));
    }

    @PostMapping("/tokens")
    public ResponseEntity<?> createPAT(@RequestParam Long userId, @RequestParam(defaultValue = "*") String abilities) {
        String token = auth.createPAT(userId, "custom", abilities);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of("token", token)));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7).trim();

        Optional<User> user = auth.getUserFromToken(token);


        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Invalid or expired token");
        }

        return ResponseEntity.ok(ApiResponse.success(user.get()));
    }

     @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(ApiResponse.badRequest("Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7).trim();
        auth.logoutPAT(token);


        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @DeleteMapping("/")
    public ResponseEntity<?> delete(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7).trim();

        Optional<User> user = auth.getUserFromToken(token);


        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Invalid or expired token");
        }

        auth.delete(user.get());

        return (ResponseEntity<?>) ResponseEntity.noContent();
    }

}
