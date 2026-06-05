package gov.rw.javane.dto.customer;

import gov.rw.javane.domain.enums.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CustomerStatusUpdateRequest")
public record CustomerStatusUpdateRequest(
        @NotNull(message = "Status is required")
        @Schema(example = "INACTIVE")
        CustomerStatus status
) {}
