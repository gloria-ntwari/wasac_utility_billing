package gov.rw.javane.dto.tariff;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TariffTierRequest(
        @NotNull(message = "From units is required")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal fromUnits,

        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal toUnits,

        @NotNull(message = "Rate per unit is required")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal ratePerUnit
) {}
