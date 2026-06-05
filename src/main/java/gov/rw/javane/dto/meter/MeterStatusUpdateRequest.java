package gov.rw.javane.dto.meter;

import gov.rw.javane.domain.enums.MeterStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MeterStatusUpdateRequest")
public record MeterStatusUpdateRequest(
        @NotNull(message = "Status is required")
        @Schema(example = "INACTIVE")
        MeterStatus status
) {}
