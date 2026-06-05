package gov.rw.javane.dto.user;

import gov.rw.javane.common.validation.ValidationPatterns;
import gov.rw.javane.domain.enums.RoleName;
import gov.rw.javane.domain.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "UserCreateRequest", description = "Admin creates staff — temporary password is auto-generated and emailed")
public record UserCreateRequest(
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
        @Schema(example = "0788123456")
        String phoneNumber,

        @NotNull(message = "Role is required")
        @Schema(example = "ROLE_OPERATOR")
        RoleName role,

        @Schema(example = "ACTIVE")
        UserStatus status
) {}
