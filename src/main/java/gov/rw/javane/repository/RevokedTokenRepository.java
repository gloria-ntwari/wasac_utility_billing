package gov.rw.javane.repository;

import gov.rw.javane.domain.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, UUID> {
    Optional<RevokedToken> findByToken(String token);
    void deleteByExpiresAtBefore(Instant instant);
}
