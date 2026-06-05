package gov.rw.javane.controller;

import gov.rw.javane.common.api.ApiResponse;
import gov.rw.javane.dto.auth.AuthResponse;
import gov.rw.javane.dto.auth.LoginRequest;
import gov.rw.javane.dto.auth.SignupRequest;
import gov.rw.javane.dto.auth.VerifyOtpRequest;
import gov.rw.javane.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, signup, and email verification")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "Customer self-registration — Done by Customer (public)")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response.message(), response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT — Done by any user (public)")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response.message(), response));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify email OTP before first staff login — Done by new staff member (public)")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Email verified successfully. You can now login with your credentials."));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke JWT — Done by any logged-in user")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.ok("Logout successful. Token has been revoked."));
    }
}
