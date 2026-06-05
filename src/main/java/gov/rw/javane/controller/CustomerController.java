package gov.rw.javane.controller;

import gov.rw.javane.common.api.ApiResponse;
import gov.rw.javane.dto.customer.CustomerRequest;
import gov.rw.javane.dto.customer.CustomerResponse;
import gov.rw.javane.dto.customer.CustomerStatusUpdateRequest;
import gov.rw.javane.service.CustomerService;
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
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer records management")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register customer record — Done by Admin")
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Customer created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    @Operation(summary = "List all customers — Done by Admin, Operator, or Finance")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok("Customers retrieved successfully", customerService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    @Operation(summary = "View customer by ID — Done by Admin, Operator, or Finance")
    public ResponseEntity<ApiResponse<CustomerResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Customer retrieved successfully", customerService.findById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update customer details — Done by Admin")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(@PathVariable UUID id,
                                                                @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Customer updated successfully", customerService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate or deactivate customer — Done by Admin")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateStatus(@PathVariable UUID id,
                                                                        @Valid @RequestBody CustomerStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Customer status updated successfully",
                customerService.updateStatus(id, request.status())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete customer — Done by Admin")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        customerService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer deleted successfully"));
    }
}
