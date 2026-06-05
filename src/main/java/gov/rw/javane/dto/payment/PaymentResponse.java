package gov.rw.javane.dto.payment;

import gov.rw.javane.domain.enums.BillStatus;
import gov.rw.javane.domain.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID billId,
        UUID customerId,
        BigDecimal amountPaid,
        PaymentMethod paymentMethod,
        LocalDate paymentDate,
        String referenceNumber,
        BillStatus billStatus,
        BigDecimal outstandingBalance,
        Instant createdAt
) {}
