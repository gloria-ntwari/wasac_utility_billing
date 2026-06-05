package gov.rw.javane.dto.bill;

import gov.rw.javane.domain.enums.MeterStatus;
import gov.rw.javane.domain.enums.MeterType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "BillMeterSummary")
public record BillMeterSummary(
        UUID id,
        String meterNumber,
        MeterType meterType,
        MeterStatus status
) {}
