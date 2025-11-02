package com.ckt.api.user.repository;

import com.ckt.api.user.model.entity.PersonalAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PATRepository extends JpaRepository<PersonalAccessToken, Long> {
    Optional<PersonalAccessToken> findByTokenHash(String tokenHash);
    List<PersonalAccessToken> findAllByTokenHashAndExpiresAtAfterOrExpiresAtIsNull(String tokenHash, Instant now);
    @Query("SELECT t FROM PersonalAccessToken t WHERE t.tokenHash = :tokenHash AND (t.expiresAt IS NULL OR t.expiresAt > :now) ORDER BY t.createdAt DESC")
    Optional<PersonalAccessToken> findFirstValidToken(@Param("tokenHash") String tokenHash, @Param("now") Instant now);

}
