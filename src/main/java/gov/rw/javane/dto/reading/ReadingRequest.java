package gov.rw.javane.dto.reading;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "ReadingRequest")
public record ReadingRequest(
        @NotNull(message = "Meter ID is required")
        @Schema(example = "550e8400-e29b-41d4-a716-446655440001")
        UUID meterId,

        @NotNull(message = "Previous reading is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Previous reading cannot be negative")
        @Schema(example = "120.0")
        BigDecimal previousReading,

        @NotNull(message = "Current reading is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Current reading cannot be negative")
        @Schema(example = "145.0")
        BigDecimal currentReading,

        @NotNull(message = "Reading date is required")
        @PastOrPresent(message = "Reading date cannot be in the future")
        @Schema(example = "2026-05-31")
        LocalDate readingDate
) {}
