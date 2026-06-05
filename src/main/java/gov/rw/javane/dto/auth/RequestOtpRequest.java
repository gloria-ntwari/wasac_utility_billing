package gov.rw.javane.dto.auth;

import gov.rw.javane.common.validation.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "RequestOtpRequest", description = "Request or resend email OTP before first login")
public record RequestOtpRequest(
        @NotBlank(message = "Email is required")
        @Pattern(regexp = ValidationPatterns.EMAIL_LOWERCASE, message = "Email must be lowercase and valid")
        @Schema(example = "customer@example.com")
        String email
) {}
