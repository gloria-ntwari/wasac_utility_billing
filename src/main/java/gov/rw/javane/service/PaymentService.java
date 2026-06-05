package gov.rw.javane.service;

import gov.rw.javane.common.exception.BadRequestException;
import gov.rw.javane.common.exception.ForbiddenException;
import gov.rw.javane.common.exception.NotFoundException;
import gov.rw.javane.domain.entity.AppUser;
import gov.rw.javane.domain.entity.Bill;
import gov.rw.javane.domain.entity.Customer;
import gov.rw.javane.domain.entity.Payment;
import gov.rw.javane.domain.enums.BillStatus;
import gov.rw.javane.domain.enums.RoleName;
import gov.rw.javane.dto.payment.PaymentRequest;
import gov.rw.javane.dto.payment.PaymentResponse;
import gov.rw.javane.mapper.EntityMapper;
import gov.rw.javane.repository.BillRepository;
import gov.rw.javane.repository.CustomerRepository;
import gov.rw.javane.repository.PaymentRepository;
import gov.rw.javane.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final BillNotificationService billNotificationService;
    private final SecurityUtils securityUtils;

    @Transactional
    public PaymentResponse record(PaymentRequest request) {
        Bill bill = billRepository.findById(request.billId())
                .orElseThrow(() -> new NotFoundException("Bill not found with id: " + request.billId()));

        assertPaymentAccess(bill);

        if (bill.getStatus() != BillStatus.APPROVED && bill.getStatus() != BillStatus.PAID) {
            throw new BadRequestException("Payments can only be recorded for approved bills. Current status: " + bill.getStatus());
        }
        if (bill.getStatus() == BillStatus.PAID && bill.getOutstandingBalance().compareTo(BigDecimal.ZERO) == 0) {
            throw new BadRequestException("Bill is already fully paid");
        }
        if (request.amountPaid().compareTo(bill.getOutstandingBalance()) > 0) {
            throw new BadRequestException("Payment amount (" + request.amountPaid()
                    + ") exceeds outstanding balance (" + bill.getOutstandingBalance() + ")");
        }

        Payment payment = Payment.builder()
                .bill(bill)
                .amountPaid(request.amountPaid())
                .paymentMethod(request.paymentMethod())
                .paymentDate(request.paymentDate())
                .referenceNumber(request.referenceNumber())
                .recordedBy(securityUtils.currentUser())
                .build();
        payment = paymentRepository.save(payment);

        // DB trigger updates outstanding balance and PAID status; refresh entity
        bill = billRepository.findById(bill.getId()).orElse(bill);

        if (bill.getOutstandingBalance().compareTo(BigDecimal.ZERO) == 0) {
            bill.setStatus(BillStatus.PAID);
            billRepository.save(bill);
        }

        bill = billRepository.findByIdWithDetails(bill.getId()).orElse(bill);
        billNotificationService.notifyPaymentReceived(
                bill, request.amountPaid(), bill.getOutstandingBalance());

        payment.setBill(bill);
        return EntityMapper.toPaymentResponse(payment);
    }

    public List<PaymentResponse> findAll() {
        AppUser user = securityUtils.currentUser();
        if (hasRole(user, RoleName.ROLE_CUSTOMER)) {
            Customer customer = customerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Customer profile not found"));
            return paymentRepository.findByBillCustomerId(customer.getId()).stream()
                    .map(EntityMapper::toPaymentResponse).toList();
        }
        return paymentRepository.findAll().stream().map(EntityMapper::toPaymentResponse).toList();
    }

    public PaymentResponse findById(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id));
        assertCustomerAccess(payment);
        return EntityMapper.toPaymentResponse(payment);
    }

    public List<PaymentResponse> findByBill(UUID billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new NotFoundException("Bill not found with id: " + billId));
        assertCustomerAccessToBill(bill);
        return paymentRepository.findByBillId(billId).stream().map(EntityMapper::toPaymentResponse).toList();
    }

    @Transactional
    public void delete(UUID id) {
        throw new BadRequestException("Payments cannot be deleted to preserve financial records.");
    }

    private void assertPaymentAccess(Bill bill) {
        AppUser user = securityUtils.currentUser();
        if (hasRole(user, RoleName.ROLE_CUSTOMER)) {
            Customer customer = customerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Customer profile not found"));
            if (!bill.getCustomer().getId().equals(customer.getId())) {
                throw new ForbiddenException("You can only pay your own bills");
            }
        }
    }

    private void assertCustomerAccess(Payment payment) {
        assertCustomerAccessToBill(payment.getBill());
    }

    private void assertCustomerAccessToBill(Bill bill) {
        AppUser user = securityUtils.currentUser();
        if (hasRole(user, RoleName.ROLE_CUSTOMER)) {
            Customer customer = customerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Customer profile not found"));
            if (!bill.getCustomer().getId().equals(customer.getId())) {
                throw new ForbiddenException("You can only view your own payment history");
            }
        }
    }

    private boolean hasRole(AppUser user, RoleName roleName) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == roleName);
    }
}
