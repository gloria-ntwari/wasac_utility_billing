package gov.rw.javane.dto.user;

import gov.rw.javane.domain.enums.RoleName;
import gov.rw.javane.domain.enums.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserCreateResponse(
        UUID id,
        String fullNames,
        String email,
        String phoneNumber,
        UserStatus status,
        boolean emailVerified,
        Set<RoleName> roles,
        Instant createdAt,
        boolean credentialsEmailSent,
        String message
) {}
