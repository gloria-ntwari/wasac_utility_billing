package gov.rw.javane.service;

import gov.rw.javane.common.exception.BadRequestException;
import gov.rw.javane.domain.entity.*;
import gov.rw.javane.domain.enums.BillStatus;
import gov.rw.javane.domain.enums.CustomerStatus;
import gov.rw.javane.repository.BillRepository;
import gov.rw.javane.repository.MeterReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillRepository billRepository;
    private final TariffService tariffService;
    private final BillingCalculatorService billingCalculatorService;
    private final MeterReadingRepository meterReadingRepository;

    @Transactional
    public Bill generateBill(Customer customer, Meter meter, MeterReading reading) {
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BadRequestException("Inactive customers cannot receive bills. Customer '"
                    + customer.getFullName() + "' is inactive.");
        }
        if (meter.getCustomer() == null || !meter.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Meter does not belong to the specified customer");
        }
        if (!reading.getMeter().getId().equals(meter.getId())) {
            throw new BadRequestException("Reading does not belong to the specified meter");
        }
        if (billRepository.findByReadingId(reading.getId()).isPresent()) {
            throw new BadRequestException("A bill already exists for reading id: " + reading.getId());
        }
        if (billRepository.existsByMeterIdAndBillingMonthAndBillingYear(
                meter.getId(), reading.getBillingMonth(), reading.getBillingYear())) {
            throw new BadRequestException("Cannot generate a bill for "
                    + String.format("%02d/%d", reading.getBillingMonth(), reading.getBillingYear())
                    + " — a bill already exists for this meter.");
        }
        if (reading.getReadingDate().isBefore(meter.getInstallationDate())) {
            throw new BadRequestException("Reading date (" + reading.getReadingDate()
                    + ") cannot be before meter installation date (" + meter.getInstallationDate() + ")");
        }

        int billingMonth = reading.getBillingMonth();
        int billingYear = reading.getBillingYear();
        LocalDate billingPeriodStart = LocalDate.of(billingYear, billingMonth, 1);

        // Auto-resolve versioned tariff: meter type + billing cycle date
        // e.g. Tariff A from 2026-01-01, Tariff B from 2026-07-01 → June 2026 uses A, July 2026+ uses B
        TariffVersion tariff = tariffService.resolveApplicableTariff(meter.getMeterType(), billingPeriodStart);

        BigDecimal consumption = reading.getCurrentReading().subtract(reading.getPreviousReading());
        BigDecimal consumptionAmount = billingCalculatorService.calculateConsumptionAmount(tariff, consumption);
        BigDecimal fixedCharge = tariff.getFixedServiceCharge();
        BigDecimal subtotal = consumptionAmount.add(fixedCharge);
        BigDecimal taxAmount = billingCalculatorService.calculateTax(subtotal, tariff.getVatRate());
        BigDecimal penaltyAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(taxAmount).add(penaltyAmount);

        Bill bill = Bill.builder()
                .customer(customer)
                .meter(meter)
                .reading(reading)
                .billingMonth(billingMonth)
                .billingYear(billingYear)
                .consumption(consumption)
                .consumptionAmount(consumptionAmount)
                .fixedCharge(fixedCharge)
                .taxAmount(taxAmount)
                .penaltyAmount(penaltyAmount)
                .totalAmount(totalAmount)
                .outstandingBalance(totalAmount)
                .status(BillStatus.PENDING_APPROVAL)
                .tariffVersion(tariff)
                .build();

        return billRepository.save(bill);
    }

    @Transactional(readOnly = true)
    public MeterReading getReading(UUID readingId) {
        return meterReadingRepository.findByIdWithMeter(readingId)
                .orElseThrow(() -> new BadRequestException("Reading not found with id: " + readingId));
    }
}
