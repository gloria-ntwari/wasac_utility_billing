package gov.rw.javane.dto.reading;

import gov.rw.javane.dto.meter.MeterCustomerSummary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "ReadingResponse")
public record ReadingResponse(
        UUID id,
        UUID meterId,
        String meterNumber,
        @Schema(description = "Customer assigned to the meter — null if meter is unassigned")
        MeterCustomerSummary customer,
        BigDecimal previousReading,
        BigDecimal currentReading,
        LocalDate readingDate,
        int billingMonth,
        int billingYear,
        UUID billId,
        Instant createdAt
) {}
