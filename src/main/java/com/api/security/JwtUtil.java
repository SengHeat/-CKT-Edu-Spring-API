package com.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Set;

@Component
public class JwtUtil {
    private final String secret;
    private final long ttlSeconds;

    public JwtUtil(com.api.config.AppConfig app) {
        this.secret = app.getJwt().getSecret();
        this.ttlSeconds = app.getJwt().getTtlHours() * 3600L;
    }

    public String createToken(Long userId, String email, Set<String> authorities) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(ttlSeconds)))
                .withClaim("uid", userId)
                .withClaim("email", email)
                .withArrayClaim("auth", authorities.toArray(new String[0]))
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
    }
}
