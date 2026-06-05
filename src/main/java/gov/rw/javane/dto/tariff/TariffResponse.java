package gov.rw.javane.dto.tariff;

import gov.rw.javane.domain.enums.MeterType;
import gov.rw.javane.domain.enums.TariffType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TariffResponse(
        UUID id,
        int version,
        MeterType meterType,
        TariffType tariffType,
        BigDecimal flatRate,
        BigDecimal fixedServiceCharge,
        BigDecimal vatRate,
        BigDecimal latePenaltyRate,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        List<TariffTierResponse> tiers,
        Instant createdAt
) {}
