package gov.rw.javane.dto.bill;

import gov.rw.javane.domain.enums.MeterType;
import gov.rw.javane.domain.enums.TariffType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(name = "BillTariffSummary")
public record BillTariffSummary(
        UUID id,
        int version,
        MeterType meterType,
        TariffType tariffType,
        BigDecimal flatRate,
        BigDecimal fixedServiceCharge,
        BigDecimal vatRate,
        BigDecimal latePenaltyRate
) {}
