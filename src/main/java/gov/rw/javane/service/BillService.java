package gov.rw.javane.service;

import gov.rw.javane.common.exception.BadRequestException;
import gov.rw.javane.common.exception.ForbiddenException;
import gov.rw.javane.common.exception.NotFoundException;
import gov.rw.javane.domain.entity.AppUser;
import gov.rw.javane.domain.entity.Bill;
import gov.rw.javane.domain.entity.Customer;
import gov.rw.javane.domain.entity.Meter;
import gov.rw.javane.domain.entity.MeterReading;
import gov.rw.javane.domain.enums.BillStatus;
import gov.rw.javane.domain.enums.RoleName;
import gov.rw.javane.dto.bill.BillGenerateRequest;
import gov.rw.javane.dto.bill.BillResponse;
import gov.rw.javane.mapper.EntityMapper;
import gov.rw.javane.repository.BillRepository;
import gov.rw.javane.repository.CustomerRepository;
import gov.rw.javane.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final BillingService billingService;
    private final BillNotificationService billNotificationService;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<BillResponse> findAllBills() {
        assertStaffOnly();
        return mapBills(billRepository.findAllWithDetails());
    }

    @Transactional(readOnly = true)
    public List<BillResponse> findByCustomerId(UUID customerId) {
        assertStaffCustomerFilterAccess(customerId);
        customerService.getCustomer(customerId);
        return mapBills(billRepository.findByCustomerIdWithDetails(customerId));
    }

    @Transactional(readOnly = true)
    public List<BillResponse> findAll(UUID customerId) {
        if (customerId != null) {
            return findByCustomerId(customerId);
        }

        AppUser user = securityUtils.currentUser();
        if (hasRole(user, RoleName.ROLE_CUSTOMER)) {
            Customer customer = resolveCurrentCustomer(user);
            return mapBills(billRepository.findByCustomerIdWithDetails(customer.getId()));
        }

        assertStaffOnly();
        return mapBills(billRepository.findAllWithDetails());
    }

    @Transactional(readOnly = true)
    public BillResponse findById(UUID id) {
        Bill bill = billRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Bill not found with id: " + id));
        assertCustomerAccess(bill);
        return EntityMapper.toBillResponse(bill);
    }

    @Transactional(readOnly = true)
    public List<BillResponse> findByStatus(BillStatus status) {
        assertStaffOnly();
        return mapBills(billRepository.findByStatusWithDetails(status));
    }

    @Transactional
    public BillResponse generate(BillGenerateRequest request) {
        MeterReading reading = billingService.getReading(request.readingId());
        Meter meter = reading.getMeter();
        if (meter == null) {
            throw new BadRequestException("Reading is not linked to a meter");
        }
        Customer customer = meter.getCustomer();
        if (customer == null) {
            throw new BadRequestException("Meter is not assigned to a customer");
        }

        Bill bill = billingService.generateBill(customer, meter, reading);
        Bill saved = billRepository.findByIdWithDetails(bill.getId())
                .orElseThrow(() -> new NotFoundException("Bill not found after generation"));
        return EntityMapper.toBillResponse(saved);
    }

    @Transactional
    public BillResponse approve(UUID id) {
        assertFinanceOnly();
        Bill bill = getBill(id);
        if (bill.getStatus() != BillStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Only bills with status PENDING_APPROVAL can be approved. Current status: " + bill.getStatus());
        }
        bill.setStatus(BillStatus.APPROVED);
        bill.setApprovedBy(securityUtils.currentUser());
        bill.setApprovedAt(Instant.now());
        billRepository.save(bill);
        Bill approved = billRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Bill not found after approval"));
        billNotificationService.notifyBillApproved(approved);
        return EntityMapper.toBillResponse(approved);
    }

    @Transactional
    public BillResponse reject(UUID id) {
        Bill bill = getBill(id);
        if (bill.getStatus() != BillStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Only bills with status PENDING_APPROVAL can be rejected");
        }
        bill.setStatus(BillStatus.REJECTED);
        billRepository.save(bill);
        return toResponse(id);
    }

    @Transactional
    public void delete(UUID id) {
        Bill bill = getBill(id);
        if (bill.getStatus() == BillStatus.PAID) {
            throw new BadRequestException("Paid bills cannot be deleted");
        }
        billRepository.delete(bill);
    }

    public Bill getBill(UUID id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bill not found with id: " + id));
    }

    private BillResponse toResponse(UUID billId) {
        return billRepository.findByIdWithDetails(billId)
                .map(EntityMapper::toBillResponse)
                .orElseThrow(() -> new NotFoundException("Bill not found with id: " + billId));
    }

    private List<BillResponse> mapBills(List<Bill> bills) {
        return bills.stream().map(EntityMapper::toBillResponse).toList();
    }

    private void assertCustomerAccess(Bill bill) {
        AppUser user = securityUtils.currentUser();
        if (hasRole(user, RoleName.ROLE_CUSTOMER)) {
            Customer customer = resolveCurrentCustomer(user);
            if (!bill.getCustomer().getId().equals(customer.getId())) {
                throw new ForbiddenException("You can only view your own bills");
            }
        }
    }

    private void assertStaffCustomerFilterAccess(UUID customerId) {
        AppUser user = securityUtils.currentUser();
        if (hasRole(user, RoleName.ROLE_CUSTOMER)) {
            Customer customer = resolveCurrentCustomer(user);
            if (!customer.getId().equals(customerId)) {
                throw new ForbiddenException("You can only view your own bills");
            }
        }
    }

    private void assertStaffOnly() {
        AppUser user = securityUtils.currentUser();
        if (hasRole(user, RoleName.ROLE_CUSTOMER)) {
            throw new ForbiddenException("Only Admin or Finance can list all bills");
        }
    }

    private void assertFinanceOnly() {
        AppUser user = securityUtils.currentUser();
        if (!hasRole(user, RoleName.ROLE_FINANCE)) {
            throw new ForbiddenException("Only Finance can approve bills");
        }
    }

    private Customer resolveCurrentCustomer(AppUser user) {
        return customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Customer profile not found for current user"));
    }

    private boolean hasRole(AppUser user, RoleName roleName) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == roleName);
    }
}
