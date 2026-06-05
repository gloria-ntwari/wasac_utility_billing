package gov.rw.javane.controller;

import gov.rw.javane.common.api.ApiResponse;
import gov.rw.javane.dto.user.AdminUserUpdateRequest;
import gov.rw.javane.dto.user.ProfileUpdateRequest;
import gov.rw.javane.dto.user.UserCreateRequest;
import gov.rw.javane.dto.user.UserCreateResponse;
import gov.rw.javane.dto.user.UserResponse;
import gov.rw.javane.service.UserService;
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
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Staff user management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Operator or Finance user — Done by Admin (temporary password emailed automatically)")
    public ResponseEntity<ApiResponse<UserCreateResponse>> create(@Valid @RequestBody UserCreateRequest request) {
        UserCreateResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response.message(), response));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "View own profile — Done by any logged-in user")
    public ResponseEntity<ApiResponse<UserResponse>> findMe() {
        return ResponseEntity.ok(ApiResponse.ok("Profile retrieved successfully", userService.findMe()));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update own profile — Done by any logged-in user (cannot change role or others' profiles)")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(@Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", userService.updateMyProfile(request)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users — Done by Admin")
    public ResponseEntity<ApiResponse<List<UserResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok("Users retrieved successfully", userService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "View user by ID — Done by Admin (any user) or by the user themselves")
    public ResponseEntity<ApiResponse<UserResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("User retrieved successfully", userService.findById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update any user including role/status — Done by Admin only")
    public ResponseEntity<ApiResponse<UserResponse>> updateByAdmin(@PathVariable UUID id,
                                                                   @Valid @RequestBody AdminUserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User updated successfully", userService.updateByAdmin(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user — Done by Admin")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted successfully"));
    }
}
