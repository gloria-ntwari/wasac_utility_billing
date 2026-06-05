package gov.rw.javane.dto.bill;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "BillReadingSummary")
public record BillReadingSummary(
        UUID id,
        BigDecimal previousReading,
        BigDecimal currentReading,
        BigDecimal unitsConsumed,
        LocalDate readingDate,
        int billingMonth,
        int billingYear
) {}
