package gov.rw.javane.security;

import gov.rw.javane.domain.entity.RevokedToken;
import gov.rw.javane.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;

    public boolean isBlacklisted(String token) {
        return revokedTokenRepository.findByToken(token).isPresent();
    }

    @Transactional
    public void revoke(String token, Instant expiresAt) {
        if (revokedTokenRepository.findByToken(token).isPresent()) {
            return;
        }
        revokedTokenRepository.save(RevokedToken.builder()
                .token(token)
                .expiresAt(expiresAt)
                .build());
        revokedTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
