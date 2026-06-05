package gov.rw.javane.dto.auth;

import gov.rw.javane.common.validation.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "LoginRequest")
public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Pattern(regexp = ValidationPatterns.EMAIL_LOWERCASE, message = "Email must be lowercase and in a valid format")
        @Schema(description = "Account email", example = "admin@example.com")
        String email,

        @NotBlank(message = "Password is required")
        @Schema(description = "Account password", example = "Admin@12345")
        String password
) {}
