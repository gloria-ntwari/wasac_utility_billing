package gov.rw.javane.controller;

import gov.rw.javane.common.api.ApiResponse;
import gov.rw.javane.domain.enums.BillStatus;
import gov.rw.javane.dto.payment.PaymentRequest;
import gov.rw.javane.dto.payment.PaymentResponse;
import gov.rw.javane.service.PaymentService;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "Record payment — Customer pays own approved bill, or Admin/Finance records on behalf")
    public ResponseEntity<ApiResponse<PaymentResponse>> record(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.record(request);
        String message = response.billStatus() == BillStatus.PAID
                && response.outstandingBalance().compareTo(BigDecimal.ZERO) == 0
                ? "Bill status automatically updated to PAID — notification triggered"
                : "Payment recorded successfully";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(message, response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "List payments — filter by billId query param")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> findAll(
            @RequestParam(required = false) UUID billId) {
        if (billId != null) {
            return ResponseEntity.ok(ApiResponse.ok("Bill payments retrieved successfully",
                    paymentService.findByBill(billId)));
        }
        return ResponseEntity.ok(ApiResponse.ok("Payments retrieved successfully", paymentService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Payment retrieved successfully", paymentService.findById(id)));
    }

    @GetMapping("/bill/{billId}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> findByBill(@PathVariable UUID billId) {
        return ResponseEntity.ok(ApiResponse.ok("Bill payments retrieved successfully", paymentService.findByBill(billId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        paymentService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Payment deleted successfully"));
    }
}
