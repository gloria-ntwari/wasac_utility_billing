package gov.rw.javane.dto.payment;

import gov.rw.javane.domain.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "PaymentRequest")
public record PaymentRequest(
        @NotNull(message = "Bill ID is required")
        @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
        UUID billId,

        @NotNull(message = "Amount paid is required")
        @DecimalMin(value = "0.01", message = "Amount paid must be greater than zero")
        @Schema(example = "5000.00")
        BigDecimal amountPaid,

        @NotNull(message = "Payment method is required")
        @Schema(example = "MOBILE_MONEY")
        PaymentMethod paymentMethod,

        @NotNull(message = "Payment date is required")
        @PastOrPresent(message = "Payment date cannot be in the future")
        @Schema(example = "2026-05-10")
        LocalDate paymentDate,

        @Size(max = 100, message = "Reference number must not exceed 100 characters")
        @Schema(example = "MM-20260510-001")
        String referenceNumber
) {}
