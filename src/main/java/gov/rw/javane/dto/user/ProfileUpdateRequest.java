package gov.rw.javane.dto.user;

import gov.rw.javane.common.validation.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "ProfileUpdateRequest", description = "Update your own profile — name, email, and phone only")
public record ProfileUpdateRequest(
        @NotBlank(message = "Full names are required")
        @Size(min = 2, max = 120)
        @Schema(example = "Jean Operator")
        String fullNames,

        @NotBlank(message = "Email is required")
        @Pattern(regexp = ValidationPatterns.EMAIL_LOWERCASE, message = "Email must be lowercase and valid")
        @Schema(example = "operator@example.com")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = ValidationPatterns.PHONE_TEN_DIGITS, message = "Phone number must be exactly 10 digits")
        @Schema(example = "250788123456")
        String phoneNumber
) {}
