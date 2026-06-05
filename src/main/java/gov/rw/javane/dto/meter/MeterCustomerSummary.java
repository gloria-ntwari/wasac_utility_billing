package gov.rw.javane.dto.meter;

import gov.rw.javane.domain.enums.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "MeterCustomerSummary", description = "Customer assigned to this meter")
public record MeterCustomerSummary(
        @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(example = "Eric Niyonzima")
        String fullName,
        @Schema(example = "1199780012345678")
        String nationalId,
        @Schema(example = "eric@gmail.com")
        String email,
        @Schema(example = "0784567890")
        String phoneNumber,
        @Schema(example = "KG 45 St, Kigali")
        String address,
        @Schema(example = "ACTIVE")
        CustomerStatus status
) {}
