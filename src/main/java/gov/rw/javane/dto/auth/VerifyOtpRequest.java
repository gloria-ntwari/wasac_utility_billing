package gov.rw.javane.dto.auth;

import gov.rw.javane.common.validation.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "VerifyOtpRequest", description = "Verify the 6-digit OTP sent to staff email after account creation")
public class VerifyOtpRequest {

    @NotBlank(message = "Email is required")
    @Pattern(regexp = ValidationPatterns.EMAIL_LOWERCASE, message = "Email must be lowercase and valid")
    @Schema(description = "Staff email address", example = "staff@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must contain exactly 6 digits")
    @Schema(description = "6-digit OTP from the credentials email", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String otp;
}
