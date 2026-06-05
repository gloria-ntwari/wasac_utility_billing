package gov.rw.javane.dto.customer;

import gov.rw.javane.common.validation.ValidationPatterns;
import gov.rw.javane.domain.enums.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "CustomerRequest")
public record CustomerRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 120)
        @Schema(example = "Eric Customer")
        String fullName,

        @NotBlank(message = "National ID is required")
        @Pattern(regexp = ValidationPatterns.NATIONAL_ID_16, message = "National ID must be exactly 16 digits")
        @Schema(example = "1199780012345678")
        String nationalId,

        @NotBlank(message = "Email is required")
        @Pattern(regexp = ValidationPatterns.EMAIL_LOWERCASE, message = "Email must be lowercase and valid")
        @Schema(example = "customer@example.com")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = ValidationPatterns.PHONE_TEN_DIGITS, message = "Phone number must be exactly 10 digits")
        @Schema(example = "0788123456")
        String phoneNumber,

        @NotBlank(message = "Address is required")
        @Size(min = 5, max = 255)
        @Schema(example = "KG 45 St, Kigali")
        String address,

        @Schema(example = "ACTIVE")
        CustomerStatus status
) {}
