package gov.rw.javane.dto.bill;

import gov.rw.javane.domain.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "BillPaymentSummary")
public record BillPaymentSummary(
        UUID id,
        BigDecimal amountPaid,
        PaymentMethod paymentMethod,
        LocalDate paymentDate,
        String referenceNumber,
        Instant createdAt
) {}
