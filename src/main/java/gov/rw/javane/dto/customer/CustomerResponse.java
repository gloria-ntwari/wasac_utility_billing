package gov.rw.javane.dto.customer;

import gov.rw.javane.domain.enums.CustomerStatus;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String fullName,
        String nationalId,
        String email,
        String phoneNumber,
        String address,
        CustomerStatus status,
        UUID userId,
        Instant createdAt
) {}
