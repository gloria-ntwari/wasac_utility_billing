package gov.rw.javane.dto.bill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(name = "BillGenerateRequest", description = """
        Generate a bill from a captured reading.
        Tariff is resolved automatically from the meter type and reading billing month/year
        (versioned tariffs — e.g. June 2026 uses tariff effective before July 2026).
        """)
public record BillGenerateRequest(
        @NotNull(message = "Customer ID is required")
        @Schema(description = "Customer UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID customerId,

        @NotNull(message = "Meter ID is required")
        @Schema(description = "Meter UUID — meter type (WATER/ELECTRICITY) determines which tariff applies",
                example = "550e8400-e29b-41d4-a716-446655440001")
        UUID meterId,

        @NotNull(message = "Reading ID is required")
        @Schema(description = "Captured meter reading UUID — billing month/year selects tariff version",
                example = "550e8400-e29b-41d4-a716-446655440002")
        UUID readingId
) {}
