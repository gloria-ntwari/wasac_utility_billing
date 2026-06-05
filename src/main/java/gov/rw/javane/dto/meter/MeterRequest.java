package gov.rw.javane.dto.meter;

import gov.rw.javane.domain.enums.MeterStatus;
import gov.rw.javane.domain.enums.MeterType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "MeterRequest")
public record MeterRequest(
        @NotBlank(message = "Meter number is required")
        @Size(min = 3, max = 50)
        @Schema(example = "WTR-001-KGL")
        String meterNumber,

        @NotNull(message = "Meter type is required")
        @Schema(example = "WATER")
        MeterType meterType,

        @NotNull(message = "Installation date is required")
        @PastOrPresent(message = "Installation date cannot be in the future")
        @Schema(example = "2026-01-15")
        LocalDate installationDate,

        @Schema(description = "Assign meter to customer on creation", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID customerId,

        @Schema(example = "ACTIVE")
        MeterStatus status
) {}
