package gov.rw.javane.dto.tariff;

import gov.rw.javane.domain.enums.MeterType;
import gov.rw.javane.domain.enums.TariffType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "TariffRequest")
public record TariffRequest(
        @NotNull(message = "Meter type is required")
        @Schema(example = "WATER")
        MeterType meterType,

        @NotNull(message = "Tariff type is required")
        @Schema(example = "FLAT")
        TariffType tariffType,

        @DecimalMin(value = "0.0", message = "Flat rate cannot be negative")
        @Schema(description = "Required for FLAT type", example = "350.00")
        BigDecimal flatRate,

        @NotNull(message = "Fixed service charge is required")
        @DecimalMin(value = "0.0", message = "Fixed service charge cannot be negative")
        @Schema(example = "1500.00")
        BigDecimal fixedServiceCharge,

        @NotNull(message = "VAT rate is required")
        @DecimalMin(value = "0.0", message = "VAT rate cannot be negative")
        @Schema(example = "18.0")
        BigDecimal vatRate,

        @NotNull(message = "Late penalty rate is required")
        @DecimalMin(value = "0.0", message = "Late penalty rate cannot be negative")
        @Schema(example = "5.0")
        BigDecimal latePenaltyRate,

        @NotNull(message = "Effective from date is required")
        @Schema(example = "2026-01-01")
        LocalDate effectiveFrom,

        @Valid
        @Size(max = 10, message = "Maximum 10 tiers allowed")
        @Schema(description = "Required for TIER type — or add later via POST /tariffs/{id}/tiers")
        List<TariffTierRequest> tiers
) {}
