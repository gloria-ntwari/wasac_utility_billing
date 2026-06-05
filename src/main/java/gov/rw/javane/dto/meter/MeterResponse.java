package gov.rw.javane.dto.meter;

import gov.rw.javane.domain.enums.MeterStatus;
import gov.rw.javane.domain.enums.MeterType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "MeterResponse")
public record MeterResponse(
        @Schema(example = "7bf28c69-2705-431d-933c-3f44520b1cc5")
        UUID id,
        @Schema(example = "WTR-001-KGL")
        String meterNumber,
        @Schema(example = "WATER")
        MeterType meterType,
        @Schema(example = "2026-01-15")
        LocalDate installationDate,
        @Schema(example = "ACTIVE")
        MeterStatus status,
        @Schema(description = "Assigned customer details — null if meter is unassigned")
        MeterCustomerSummary customer,
        Instant createdAt,
        Instant updatedAt
) {}
