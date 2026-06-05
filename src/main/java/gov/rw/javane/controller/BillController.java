package gov.rw.javane.controller;

import gov.rw.javane.common.api.ApiResponse;
import gov.rw.javane.domain.enums.BillStatus;
import gov.rw.javane.dto.bill.BillGenerateRequest;
import gov.rw.javane.dto.bill.BillResponse;
import gov.rw.javane.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/bills")
@RequiredArgsConstructor
@Tag(name = "Bills", description = "Bill generation, approval, and customer viewing")
@SecurityRequirement(name = "bearerAuth")
public class BillController {

    private final BillService billService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "Generate bill — tariff auto-selected from meter type + reading billing period (versioned)")
    public ResponseEntity<ApiResponse<BillResponse>> generate(@Valid @RequestBody BillGenerateRequest request) {
        BillResponse response = billService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Bill generated — tariff applied automatically — customer notified", response));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "List ALL bills in the system — Done by Admin or Finance")
    public ResponseEntity<ApiResponse<List<BillResponse>>> findAllBills() {
        List<BillResponse> bills = billService.findAllBills();
        return ResponseEntity.ok(ApiResponse.ok(
                "All bills retrieved successfully (" + bills.size() + " found)", bills));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "List bills — use /bills/all for all; filter with ?customerId=; Customer sees own bills")
    public ResponseEntity<ApiResponse<List<BillResponse>>> findAll(
            @Parameter(description = "Optional — filter by customer UUID (Admin/Finance)", required = false)
            @RequestParam(required = false) UUID customerId) {
        List<BillResponse> bills = billService.findAll(customerId);
        String message = customerId != null
                ? "Customer bills retrieved successfully (" + bills.size() + " found)"
                : "Bills retrieved successfully (" + bills.size() + " found)";
        return ResponseEntity.ok(ApiResponse.ok(message, bills));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "View bill by ID — Done by Admin/Finance or owning Customer")
    public ResponseEntity<ApiResponse<BillResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Bill retrieved successfully", billService.findById(id)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "List bills by status — Done by Admin or Finance")
    public ResponseEntity<ApiResponse<List<BillResponse>>> findByStatus(@PathVariable BillStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Bills retrieved successfully", billService.findByStatus(status)));
    }

    @RequestMapping(value = "/{id}/approve", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Approve bill for customer payment — Done by Finance only (notifies customer by email)")
    public ResponseEntity<ApiResponse<BillResponse>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Bill approved — customer notified to pay", billService.approve(id)));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "Reject a pending bill — Done by Admin or Finance")
    public ResponseEntity<ApiResponse<BillResponse>> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Bill rejected successfully", billService.reject(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete bill — Done by Admin")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        billService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Bill deleted successfully"));
    }
}
