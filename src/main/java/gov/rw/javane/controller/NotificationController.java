package gov.rw.javane.controller;

import gov.rw.javane.common.api.ApiResponse;
import gov.rw.javane.dto.notification.NotificationResponse;
import gov.rw.javane.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "List notifications — Done by Admin/Finance (all) or Customer (own)")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> findAll(
            @RequestParam(required = false) UUID customerId) {
        return ResponseEntity.ok(ApiResponse.ok("Notifications retrieved successfully",
                notificationService.findAll(customerId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    public ResponseEntity<ApiResponse<NotificationResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Notification retrieved successfully", notificationService.findById(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        notificationService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Notification deleted successfully"));
    }
}
