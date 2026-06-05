package gov.rw.javane.dto.bill;

import gov.rw.javane.domain.enums.BillStatus;
import gov.rw.javane.dto.meter.MeterCustomerSummary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(name = "BillResponse", description = "Full bill with customer, meter, reading, tariff, and payments")
public record BillResponse(
        UUID id,
        @Schema(example = "05/2025")
        String billingPeriod,
        int billingMonth,
        int billingYear,
        BigDecimal consumption,
        BigDecimal consumptionAmount,
        BigDecimal fixedCharge,
        BigDecimal taxAmount,
        BigDecimal penaltyAmount,
        BigDecimal totalAmount,
        BigDecimal outstandingBalance,
        BillStatus status,
        MeterCustomerSummary customer,
        BillMeterSummary meter,
        BillReadingSummary reading,
        BillTariffSummary tariff,
        List<BillPaymentSummary> payments,
        int paymentCount,
        String approvedByName,
        Instant approvedAt,
        Instant createdAt,
        Instant updatedAt
) {}
