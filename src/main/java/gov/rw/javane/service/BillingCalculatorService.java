package gov.rw.javane.service;

import gov.rw.javane.common.exception.BadRequestException;
import gov.rw.javane.domain.entity.TariffTier;
import gov.rw.javane.domain.entity.TariffVersion;
import gov.rw.javane.domain.enums.TariffType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;

@Service
public class BillingCalculatorService {

    public BigDecimal calculateConsumptionAmount(TariffVersion tariff, BigDecimal consumption) {
        if (consumption.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Consumption cannot be negative");
        }
        if (tariff.getTariffType() == TariffType.FLAT) {
            if (tariff.getFlatRate() == null) {
                throw new BadRequestException("Flat rate is not configured for tariff version " + tariff.getVersion());
            }
            return consumption.multiply(tariff.getFlatRate()).setScale(2, RoundingMode.HALF_UP);
        }

        if (tariff.getTiers() == null || tariff.getTiers().isEmpty()) {
            throw new BadRequestException("Tier configuration is required for tier-based tariff version " + tariff.getVersion());
        }

        BigDecimal remaining = consumption;
        BigDecimal total = BigDecimal.ZERO;
        var sortedTiers = tariff.getTiers().stream()
                .sorted(Comparator.comparing(TariffTier::getFromUnits))
                .toList();

        for (TariffTier tier : sortedTiers) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal tierCapacity = tier.getToUnits() != null
                    ? tier.getToUnits().subtract(tier.getFromUnits())
                    : remaining;
            if (tierCapacity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal unitsInTier = remaining.min(tierCapacity);
            total = total.add(unitsInTier.multiply(tier.getRatePerUnit()));
            remaining = remaining.subtract(unitsInTier);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            TariffTier lastTier = sortedTiers.get(sortedTiers.size() - 1);
            total = total.add(remaining.multiply(lastTier.getRatePerUnit()));
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTax(BigDecimal subtotal, BigDecimal vatRate) {
        return subtotal.multiply(vatRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
