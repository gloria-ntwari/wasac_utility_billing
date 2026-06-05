package gov.rw.javane.controller;

import gov.rw.javane.common.api.ApiResponse;
import gov.rw.javane.dto.reading.ReadingRequest;
import gov.rw.javane.dto.reading.ReadingResponse;
import gov.rw.javane.service.MeterReadingService;
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
@RequestMapping("/readings")
@RequiredArgsConstructor
@Tag(name = "Meter Readings")
@SecurityRequirement(name = "bearerAuth")
public class ReadingController {

    private final MeterReadingService meterReadingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Operation(summary = "Capture meter reading — Done by Operator (no bill yet; Finance generates bill later)")
    public ResponseEntity<ApiResponse<ReadingResponse>> capture(@Valid @RequestBody ReadingRequest request) {
        ReadingResponse response = meterReadingService.capture(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Meter reading captured successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    @Operation(summary = "List all readings, or filter by meter — Done by Admin, Operator, or Finance")
    public ResponseEntity<ApiResponse<List<ReadingResponse>>> findAll(
            @Parameter(description = "Optional — omit to return ALL readings", required = false)
            @RequestParam(required = false) UUID meterId) {
        List<ReadingResponse> readings = meterReadingService.findAll(meterId);
        String message = meterId != null
                ? "Meter readings retrieved successfully (" + readings.size() + " found)"
                : "All readings retrieved successfully (" + readings.size() + " found)";
        return ResponseEntity.ok(ApiResponse.ok(message, readings));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    @Operation(summary = "View reading by ID — Done by Admin, Operator, or Finance")
    public ResponseEntity<ApiResponse<ReadingResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Reading retrieved successfully", meterReadingService.findById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<ReadingResponse>> update(@PathVariable UUID id,
                                                               @Valid @RequestBody ReadingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Reading updated", meterReadingService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        meterReadingService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Reading deleted successfully"));
    }
}
