package com.api.service;

import com.api.config.AppConfig;
import com.api.dto.RegisterRequest;
import com.api.entity.PersonalAccessToken;
import com.api.entity.User;
import com.api.repository.PATRepository;
import com.api.repository.UserRepository;
import com.api.security.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final PATRepository patRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    private final AppConfig app;

    public AuthService(UserRepository userRepo, PATRepository patRepo, PasswordEncoder encoder, JwtUtil jwt, AppConfig app) {
        this.userRepo = userRepo;
        this.patRepo = patRepo;
        this.encoder = encoder;
        this.jwt = jwt;
        this.app = app;
    }

    public User register(RegisterRequest request) {
        User u = new User();
        u.setFirstName(request.getFirstName());
        u.setLastName(request.getLastName());
        u.setUsername(request.getUsername());
        u.setEmail(request.getEmail());
        u.setPasswordHash(encoder.encode(request.getPassword()));
        u.setStatus("active");
        return userRepo.save(u);
    }

    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public Map<String, Object> loginIssueJwt(User u) {
        String token = jwt.createToken(u.getId(), u.getEmail(), u.getAllAuthorities());
        return Map.of("token", token);
    }

    public String createPAT(Long userId, String name, String abilities) {
        String plain = generatePlainToken();
        String hash = sha256(plain);
        PersonalAccessToken pat = new PersonalAccessToken();
        pat.setUserId(userId);
        pat.setName(name);
        pat.setTokenHash(hash);
        pat.setAbilities(abilities == null ? "*" : abilities);
        patRepo.save(pat);
        return pat.getId() + "|" + plain;
    }

    public Optional<PersonalAccessToken> findPATByPlainToken(String plain) {

        String[] parts = plain.split("\\|");
        if (parts.length != 2) return Optional.empty();

        long id;
        try {
            id = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        String plainToken = parts[1];
        String hash = sha256(plainToken);

        return patRepo.findById(id)
                .filter(t -> t.getTokenHash().equals(hash))
                .filter(t -> t.getExpiresAt() == null || t.getExpiresAt().isAfter(Instant.now()));

    }

    public Optional<User> getUserFromToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();

        // ---- Case 1: Laravel-style token (id|hash) ----
        if (token.contains("|")) {
            String[] parts = token.split("\\|");
            if (parts.length != 2) return Optional.empty();

            long tokenId;
            try {
                tokenId = Long.parseLong(parts[0]);
            } catch (NumberFormatException e) {
                return Optional.empty();
            }

            String plainHash = parts[1];
            return patRepo.findById(tokenId)
                    .filter(pat -> pat.getTokenHash().equals(sha256(plainHash)))
                    .filter(pat -> pat.getExpiresAt() == null || pat.getExpiresAt().isAfter(Instant.now()))
                    .map(PersonalAccessToken::getUser);
        }

        // ---- Case 2: JWT token ----
        try {
            DecodedJWT j = jwt.verify(token);
            Long userId = j.getClaim("uid").asLong();
            return userRepo.findById(userId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    public void touchPAT(PersonalAccessToken pat) {
        pat.setLastUsed(Instant.now());
        patRepo.save(pat);
    }

    public void logoutPAT(String token) {
        String[] parts = token.split("\\|");
        long tokenId = Long.parseLong(parts[0]);

        patRepo.findById(tokenId).ifPresent(pat -> {
            pat.setExpiresAt(Instant.now());
            patRepo.save(pat);
        });
    }

    public void delete(User user) {
        userRepo.delete(user);
    }

    // ===== helpers =====
    private static String generatePlainToken() {
        byte[] b = new byte[32];
        new SecureRandom().nextBytes(b);
        return HexFormat.of().formatHex(b);
    }

    public static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(s.getBytes());
            return HexFormat.of().formatHex(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
