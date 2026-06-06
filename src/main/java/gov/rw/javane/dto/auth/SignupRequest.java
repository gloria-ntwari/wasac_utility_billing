package gov.rw.javane.dto.auth;

import gov.rw.javane.common.validation.ValidationPatterns;
import gov.rw.javane.domain.enums.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "SignupRequest")
public record SignupRequest(
        @NotBlank(message = "Full names are required")
        @Size(min = 2, max = 120, message = "Full names must be between 2 and 120 characters")
        @Schema(example = "John Customer")
        String fullNames,

        @NotBlank(message = "National ID is required")
        @Pattern(regexp = ValidationPatterns.NATIONAL_ID_16, message = "National ID must be exactly 16 digits")
        @Schema(example = "1199780012345678")
        String nationalId,

        @NotBlank(message = "Email is required")
        @Pattern(regexp = ValidationPatterns.EMAIL_LOWERCASE, message = "Email must be lowercase and in a valid format (no capital letters)")
        @Schema(example = "customer@example.com")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = ValidationPatterns.PHONE_TEN_DIGITS, message = "Phone number must be exactly 10 digits")
        @Schema(example = "0788123456")
        String phoneNumber,

        @NotBlank(message = "Address is required")
        @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
        @Schema(example = "KG 15 Ave, Kigali")
        String address,

        @NotBlank(message = "Password is required")
        @Pattern(regexp = ValidationPatterns.STRONG_PASSWORD,
                message = "Password must be at least 8 characters with uppercase, lowercase, digit, special character, and no spaces")
        @Schema(example = "Customer@123")
        String password,

        @Schema(description = "Customer account status — defaults to ACTIVE if omitted", example = "ACTIVE")
        CustomerStatus status
) {}
