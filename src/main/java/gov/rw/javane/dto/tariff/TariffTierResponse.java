package gov.rw.javane.dto.tariff;

import java.math.BigDecimal;
import java.util.UUID;

public record TariffTierResponse(
        UUID id,
        BigDecimal fromUnits,
        BigDecimal toUnits,
        BigDecimal ratePerUnit
) {}
