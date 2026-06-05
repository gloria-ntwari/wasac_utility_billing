package gov.rw.javane.service;

import gov.rw.javane.domain.entity.Bill;
import gov.rw.javane.domain.entity.Customer;
import gov.rw.javane.domain.entity.Notification;
import gov.rw.javane.domain.enums.NotificationChannel;
import gov.rw.javane.domain.enums.NotificationStatus;
import gov.rw.javane.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BillNotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Transactional
    public void notifyBillGenerated(Bill bill) {
        Customer customer = bill.getCustomer();
        String period = formatBillingPeriod(bill);
        String amount = formatAmount(bill.getTotalAmount());
        String message = "Dear %s, Your %s utility bill of %s FRW has been successfully processed."
                .formatted(customer.getFullName(), period, amount);
        deliver(customer, bill, message, "Utility bill generated — " + period);
    }

    @Transactional
    public void notifyBillApproved(Bill bill) {
        Customer customer = bill.getCustomer();
        String period = formatBillingPeriod(bill);
        String amount = formatAmount(bill.getTotalAmount());
        String balance = formatAmount(bill.getOutstandingBalance());
        String message = "Dear %s, Your %s utility bill of %s FRW has been approved and is ready for payment. Outstanding balance: %s FRW."
                .formatted(customer.getFullName(), period, amount, balance);
        deliver(customer, bill, message, "Utility bill approved — please pay — " + period);
    }

    @Transactional
    public void notifyPaymentReceived(Bill bill, BigDecimal amountPaid, BigDecimal outstandingBalance) {
        Customer customer = bill.getCustomer();
        String period = formatBillingPeriod(bill);

        String message;
        String subject;
        if (outstandingBalance.compareTo(BigDecimal.ZERO) == 0) {
            message = "Dear %s, your %s payment has been received. Outstanding balance: 0 FRW."
                    .formatted(customer.getFullName(), period);
            subject = "Payment received — bill fully paid — " + period;
        } else {
            message = "Dear %s, your %s payment of %s FRW has been received. Outstanding balance: %s FRW."
                    .formatted(customer.getFullName(), period, formatAmount(amountPaid), formatAmount(outstandingBalance));
            subject = "Payment received — " + period;
        }
        deliver(customer, bill, message, subject);
    }

    private void deliver(Customer customer, Bill bill, String message, String emailSubject) {
        Notification notification = Notification.builder()
                .customer(customer)
                .bill(bill)
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.PENDING)
                .message(message)
                .build();
        notification = notificationRepository.save(notification);

        boolean emailSent = emailService.sendCustomerNotificationEmail(
                customer.getEmail(), customer.getFullName(), message, emailSubject);
        notification.setStatus(emailSent ? NotificationStatus.SENT : NotificationStatus.FAILED);
        notificationRepository.save(notification);
    }

    private String formatBillingPeriod(Bill bill) {
        return String.format("%02d/%d", bill.getBillingMonth(), bill.getBillingYear());
    }

    private String formatAmount(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }
}
