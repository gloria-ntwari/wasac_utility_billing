package gov.rw.javane.dto.bill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(name = "BillGenerateRequest", description = """
        Generate a bill from a captured meter reading.
        Customer and meter are derived from the reading.
        Tariff is resolved automatically from the meter type and reading billing month/year.
        """)
public record BillGenerateRequest(
        @NotNull(message = "Reading ID is required")
        @Schema(description = "Captured meter reading UUID — customer and meter are derived from this reading",
                example = "550e8400-e29b-41d4-a716-446655440002")
        UUID readingId
) {}
