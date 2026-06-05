package gov.rw.javane.controller;

import gov.rw.javane.common.api.ApiResponse;
import gov.rw.javane.domain.enums.MeterType;
import gov.rw.javane.dto.tariff.TariffRequest;
import gov.rw.javane.dto.tariff.TariffResponse;
import gov.rw.javane.dto.tariff.TariffTierRequest;
import gov.rw.javane.dto.tariff.TariffTierResponse;
import gov.rw.javane.service.TariffService;
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
@RequestMapping("/tariffs")
@RequiredArgsConstructor
@Tag(name = "Tariffs")
@SecurityRequirement(name = "bearerAuth")
public class TariffController {

    private final TariffService tariffService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create tariff version — Done by Admin")
    public ResponseEntity<ApiResponse<TariffResponse>> create(@Valid @RequestBody TariffRequest request) {
        TariffResponse response = tariffService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tariff version created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "List all tariffs — Done by Admin or Finance")
    public ResponseEntity<ApiResponse<List<TariffResponse>>> findAll() {
        List<TariffResponse> tariffs = tariffService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(
                "All tariffs retrieved successfully (" + tariffs.size() + " found)", tariffs));
    }

    @GetMapping("/meter-type/{meterType}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "List tariffs by meter type — WATER or ELECTRICITY")
    public ResponseEntity<ApiResponse<List<TariffResponse>>> findByMeterType(@PathVariable MeterType meterType) {
        List<TariffResponse> tariffs = tariffService.findByMeterType(meterType);
        return ResponseEntity.ok(ApiResponse.ok(
                meterType + " tariffs retrieved successfully (" + tariffs.size() + " found)", tariffs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "View tariff by ID — includes tiers for TIER type")
    public ResponseEntity<ApiResponse<TariffResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Tariff retrieved successfully", tariffService.findById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TariffResponse>> update(@PathVariable UUID id,
                                                              @Valid @RequestBody TariffRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Tariff update attempted", tariffService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        tariffService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Tariff deleted successfully"));
    }

    @PostMapping("/{id}/tiers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a tier to an existing TIER tariff version")
    public ResponseEntity<ApiResponse<TariffResponse>> addTier(@PathVariable UUID id,
                                                               @Valid @RequestBody TariffTierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tariff tier added successfully", tariffService.addTier(id, request)));
    }

    @GetMapping("/{id}/tiers")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "List tiers for a tariff version")
    public ResponseEntity<ApiResponse<List<TariffTierResponse>>> getTiers(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Tariff tiers retrieved successfully", tariffService.getTiers(id)));
    }
}
