package gov.rw.javane.dto.auth;

import gov.rw.javane.domain.enums.RoleName;

import java.util.Set;
import java.util.UUID;

public record AuthResponse(
        String token,
        String message,
        UUID userId,
        UUID customerId,
        String email,
        String fullNames,
        Set<RoleName> roles
) {}
