package gov.rw.javane.controller;

import gov.rw.javane.common.api.ApiResponse;
import gov.rw.javane.dto.meter.AssignMeterRequest;
import gov.rw.javane.dto.meter.MeterRequest;
import gov.rw.javane.dto.meter.MeterResponse;
import gov.rw.javane.dto.meter.MeterStatusUpdateRequest;
import gov.rw.javane.service.MeterService;
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
@RequestMapping("/meters")
@RequiredArgsConstructor
@Tag(name = "Meters")
@SecurityRequirement(name = "bearerAuth")
public class MeterController {

    private final MeterService meterService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Operation(summary = "Create meter and assign to customer — Done by Admin or Operator")
    public ResponseEntity<ApiResponse<MeterResponse>> create(@Valid @RequestBody MeterRequest request) {
        MeterResponse response = meterService.create(request);
        String message = request.customerId() != null
                ? "Meter created and assigned to customer successfully"
                : "Meter created successfully";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(message, response));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Operation(summary = "List ALL meters in the system — Done by Admin or Operator")
    public ResponseEntity<ApiResponse<List<MeterResponse>>> findAllMeters() {
        List<MeterResponse> meters = meterService.findAllMeters();
        return ResponseEntity.ok(ApiResponse.ok(
                "All meters retrieved successfully (" + meters.size() + " found)", meters));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','CUSTOMER')")
    @Operation(summary = "List meters — use /meters/all for all; filter with ?customerId=; Customer sees own meters")
    public ResponseEntity<ApiResponse<List<MeterResponse>>> findAll(
            @Parameter(description = "Optional — filter by customer UUID (Admin/Operator)", required = false)
            @RequestParam(required = false) UUID customerId) {
        List<MeterResponse> meters = meterService.findAll(customerId);
        String message = customerId != null
                ? "Customer meters retrieved successfully (" + meters.size() + " found)"
                : "Meters retrieved successfully (" + meters.size() + " found)";
        return ResponseEntity.ok(ApiResponse.ok(message, meters));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','CUSTOMER')")
    @Operation(summary = "View meter by ID — Admin/Operator: any meter; Customer: own meter only")
    public ResponseEntity<ApiResponse<MeterResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meter retrieved successfully", meterService.findById(id)));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','CUSTOMER')")
    @Operation(summary = "List meters for a customer — Admin/Operator: any customer; Customer: own profile only")
    public ResponseEntity<ApiResponse<List<MeterResponse>>> findByCustomer(@PathVariable UUID customerId) {
        List<MeterResponse> meters = meterService.findByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.ok(
                "Customer meters retrieved successfully (" + meters.size() + " found)", meters));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Operation(summary = "Update meter details — Done by Admin or Operator")
    public ResponseEntity<ApiResponse<MeterResponse>> update(@PathVariable UUID id,
                                                             @Valid @RequestBody MeterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meter updated successfully", meterService.update(id, request)));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Operation(summary = "Assign meter to customer — Done by Admin or Operator")
    public ResponseEntity<ApiResponse<MeterResponse>> assign(@PathVariable UUID id,
                                                             @Valid @RequestBody AssignMeterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meter assigned to customer successfully",
                meterService.assignToCustomer(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Operation(summary = "Activate or deactivate meter — Done by Admin or Operator")
    public ResponseEntity<ApiResponse<MeterResponse>> updateStatus(@PathVariable UUID id,
                                                                   @Valid @RequestBody MeterStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meter status updated successfully",
                meterService.updateStatus(id, request.status())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete meter — Done by Admin")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        meterService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Meter deleted successfully"));
    }
}
