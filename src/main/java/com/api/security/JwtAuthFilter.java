package com.api.security;

import com.api.entity.PersonalAccessToken;
import com.api.entity.User;
import com.api.exception.UnauthorizedException;
import com.api.repository.PATRepository;
import com.api.repository.UserRepository;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.api.service.AuthService.sha256;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;
    private final PATRepository personalAccessTokenRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepo, PATRepository patOpt, PATRepository personalAccessTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
        this.personalAccessTokenRepository = personalAccessTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (token.split("\\.").length == 3) {
                    // ---- JWT CASE ----
                    DecodedJWT jwt = jwtUtil.verify(token);
                    String userId = jwt.getClaim("uid").asString();
                    List<String> authList = jwt.getClaim("auth").asList(String.class);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null,
                                    authList.stream().map(SimpleGrantedAuthority::new).toList());

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } else if (token.contains("|")) {
                    String[] parts = token.split("\\|");
                    if (parts.length != 2) return;

                    long id;
                    try {
                        id = Long.parseLong(parts[0]);
                    } catch (NumberFormatException e) {
                        return;
                    }

                    String plainToken = parts[1];
                    String hash = sha256(plainToken);

                    // Save the filtered Optional
                    Optional<PersonalAccessToken> patOpt = personalAccessTokenRepository.findById(id)
                            .filter(t -> t.getTokenHash().equals(hash))
                            .filter(t -> t.getExpiresAt() == null || t.getExpiresAt().isAfter(Instant.now()));

                    patOpt.ifPresent(pat -> {
                        // Get the user associated with this token
                        User u = pat.getUser();

                        // Build authorities for Spring Security
                        Set<SimpleGrantedAuthority> authorities = u.getAllAuthorities().stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toSet());

                        // Create authentication token
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(u, null, authorities);

                        // Set the authentication in the SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        patOpt.get().setLastUsed(Instant.now());
                        personalAccessTokenRepository.save(patOpt.get());
                    });

                }
            } catch (Exception e) {
                // fallback: no auth
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

}
