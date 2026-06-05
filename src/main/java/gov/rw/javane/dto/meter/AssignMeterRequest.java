package gov.rw.javane.dto.meter;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignMeterRequest(
        @NotNull(message = "Customer ID is required")
        UUID customerId
) {}
