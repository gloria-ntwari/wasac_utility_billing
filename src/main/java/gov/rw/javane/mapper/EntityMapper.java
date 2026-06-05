package gov.rw.javane.mapper;

import gov.rw.javane.domain.entity.*;
import gov.rw.javane.domain.enums.RoleName;
import gov.rw.javane.dto.bill.BillMeterSummary;
import gov.rw.javane.dto.bill.BillPaymentSummary;
import gov.rw.javane.dto.bill.BillReadingSummary;
import gov.rw.javane.dto.bill.BillResponse;
import gov.rw.javane.dto.bill.BillTariffSummary;
import gov.rw.javane.dto.customer.CustomerResponse;
import gov.rw.javane.dto.meter.MeterCustomerSummary;
import gov.rw.javane.dto.meter.MeterResponse;
import gov.rw.javane.dto.notification.NotificationResponse;
import gov.rw.javane.dto.payment.PaymentResponse;
import gov.rw.javane.dto.reading.ReadingResponse;
import gov.rw.javane.dto.tariff.TariffResponse;
import gov.rw.javane.dto.tariff.TariffTierResponse;
import gov.rw.javane.dto.user.UserResponse;

import java.util.stream.Collectors;

public final class EntityMapper {

    private EntityMapper() {}

    public static UserResponse toUserResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getFullNames(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus(),
                user.isEmailVerified(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.getCreatedAt()
        );
    }

    public static CustomerResponse toCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getNationalId(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getAddress(),
                customer.getStatus(),
                customer.getUser() != null ? customer.getUser().getId() : null,
                customer.getCreatedAt()
        );
    }

    public static MeterResponse toMeterResponse(Meter meter) {
        return new MeterResponse(
                meter.getId(),
                meter.getMeterNumber(),
                meter.getMeterType(),
                meter.getInstallationDate(),
                meter.getStatus(),
                toCustomerSummary(meter.getCustomer()),
                meter.getCreatedAt(),
                meter.getUpdatedAt()
        );
    }

    private static MeterCustomerSummary toCustomerSummary(Customer customer) {
        if (customer == null) {
            return null;
        }
        return new MeterCustomerSummary(
                customer.getId(),
                customer.getFullName(),
                customer.getNationalId(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getAddress(),
                customer.getStatus()
        );
    }

    public static ReadingResponse toReadingResponse(MeterReading reading, Bill bill) {
        return new ReadingResponse(
                reading.getId(),
                reading.getMeter().getId(),
                reading.getMeter().getMeterNumber(),
                toCustomerSummary(reading.getMeter().getCustomer()),
                reading.getPreviousReading(),
                reading.getCurrentReading(),
                reading.getReadingDate(),
                reading.getBillingMonth(),
                reading.getBillingYear(),
                bill != null ? bill.getId() : null,
                reading.getCreatedAt()
        );
    }

    public static TariffResponse toTariffResponse(TariffVersion tariff) {
        return new TariffResponse(
                tariff.getId(),
                tariff.getVersion(),
                tariff.getMeterType(),
                tariff.getTariffType(),
                tariff.getFlatRate(),
                tariff.getFixedServiceCharge(),
                tariff.getVatRate(),
                tariff.getLatePenaltyRate(),
                tariff.getEffectiveFrom(),
                tariff.getEffectiveTo(),
                tariff.getTiers().stream()
                        .map(t -> new TariffTierResponse(t.getId(), t.getFromUnits(), t.getToUnits(), t.getRatePerUnit()))
                        .toList(),
                tariff.getCreatedAt()
        );
    }

    public static BillResponse toBillResponse(Bill bill) {
        var payments = bill.getPayments().stream()
                .map(p -> new BillPaymentSummary(
                        p.getId(),
                        p.getAmountPaid(),
                        p.getPaymentMethod(),
                        p.getPaymentDate(),
                        p.getReferenceNumber(),
                        p.getCreatedAt()))
                .toList();

        return new BillResponse(
                bill.getId(),
                formatBillingPeriod(bill.getBillingMonth(), bill.getBillingYear()),
                bill.getBillingMonth(),
                bill.getBillingYear(),
                bill.getConsumption(),
                bill.getConsumptionAmount(),
                bill.getFixedCharge(),
                bill.getTaxAmount(),
                bill.getPenaltyAmount(),
                bill.getTotalAmount(),
                bill.getOutstandingBalance(),
                bill.getStatus(),
                toCustomerSummary(bill.getCustomer()),
                toBillMeterSummary(bill.getMeter()),
                toBillReadingSummary(bill.getReading(), bill.getConsumption()),
                toBillTariffSummary(bill.getTariffVersion()),
                payments,
                payments.size(),
                bill.getApprovedBy() != null ? bill.getApprovedBy().getFullNames() : null,
                bill.getApprovedAt(),
                bill.getCreatedAt(),
                bill.getUpdatedAt()
        );
    }

    private static String formatBillingPeriod(int month, int year) {
        return String.format("%02d/%d", month, year);
    }

    private static BillMeterSummary toBillMeterSummary(Meter meter) {
        return new BillMeterSummary(
                meter.getId(),
                meter.getMeterNumber(),
                meter.getMeterType(),
                meter.getStatus()
        );
    }

    private static BillReadingSummary toBillReadingSummary(MeterReading reading, java.math.BigDecimal consumption) {
        if (reading == null) {
            return null;
        }
        return new BillReadingSummary(
                reading.getId(),
                reading.getPreviousReading(),
                reading.getCurrentReading(),
                consumption,
                reading.getReadingDate(),
                reading.getBillingMonth(),
                reading.getBillingYear()
        );
    }

    private static BillTariffSummary toBillTariffSummary(TariffVersion tariff) {
        if (tariff == null) {
            return null;
        }
        return new BillTariffSummary(
                tariff.getId(),
                tariff.getVersion(),
                tariff.getMeterType(),
                tariff.getTariffType(),
                tariff.getFlatRate(),
                tariff.getFixedServiceCharge(),
                tariff.getVatRate(),
                tariff.getLatePenaltyRate()
        );
    }

    public static PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBill().getId(),
                payment.getBill().getCustomer().getId(),
                payment.getAmountPaid(),
                payment.getPaymentMethod(),
                payment.getPaymentDate(),
                payment.getReferenceNumber(),
                payment.getBill().getStatus(),
                payment.getBill().getOutstandingBalance(),
                payment.getCreatedAt()
        );
    }

    public static NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getCustomer().getId(),
                notification.getBill() != null ? notification.getBill().getId() : null,
                notification.getChannel(),
                notification.getStatus(),
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }
}
