package gov.rw.javane.service;

import gov.rw.javane.common.exception.BadRequestException;
import gov.rw.javane.common.exception.ForbiddenException;
import gov.rw.javane.common.exception.NotFoundException;
import gov.rw.javane.domain.entity.AppUser;
import gov.rw.javane.domain.entity.Customer;
import gov.rw.javane.domain.entity.Meter;
import gov.rw.javane.domain.enums.CustomerStatus;
import gov.rw.javane.domain.enums.MeterStatus;
import gov.rw.javane.domain.enums.RoleName;
import gov.rw.javane.dto.meter.AssignMeterRequest;
import gov.rw.javane.dto.meter.MeterRequest;
import gov.rw.javane.dto.meter.MeterResponse;
import gov.rw.javane.mapper.EntityMapper;
import gov.rw.javane.repository.BillRepository;
import gov.rw.javane.repository.CustomerRepository;
import gov.rw.javane.repository.MeterReadingRepository;
import gov.rw.javane.repository.MeterRepository;
import gov.rw.javane.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeterService {

    private final MeterRepository meterRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final SecurityUtils securityUtils;

    @Transactional
    public MeterResponse create(MeterRequest request) {
        if (meterRepository.existsByMeterNumber(request.meterNumber().trim())) {
            throw new BadRequestException("Meter number already exists: " + request.meterNumber());
        }
        Meter meter = Meter.builder()
                .meterNumber(request.meterNumber().trim())
                .meterType(request.meterType())
                .installationDate(request.installationDate())
                .status(request.status() != null ? request.status() : MeterStatus.ACTIVE)
                .build();
        if (request.customerId() != null) {
            meter.setCustomer(resolveActiveCustomer(request.customerId()));
        }
        Meter saved = meterRepository.save(meter);
        return toResponse(saved.getId());
    }

    @Transactional(readOnly = true)
    public List<MeterResponse> findAllMeters() {
        assertStaffOnly();
        return mapMeters(meterRepository.findAllWithCustomer());
    }

    @Transactional(readOnly = true)
    public List<MeterResponse> findAll(UUID customerId) {
        if (customerId != null) {
            assertStaffCustomerFilterAccess(customerId);
            customerService.getCustomer(customerId);
            return mapMeters(meterRepository.findByCustomerIdWithCustomer(customerId));
        }

        AppUser user = securityUtils.currentUser();
        if (hasRole(user, RoleName.ROLE_CUSTOMER)) {
            Customer customer = resolveCurrentCustomer(user);
            return mapMeters(meterRepository.findByCustomerIdWithCustomer(customer.getId()));
        }

        assertStaffOnly();
        return mapMeters(meterRepository.findAllWithCustomer());
    }

    @Transactional(readOnly = true)
    public MeterResponse findById(UUID id) {
        Meter meter = meterRepository.findByIdWithCustomer(id)
                .orElseThrow(() -> new NotFoundException("Meter not found with id: " + id));
        assertCustomerMeterAccess(meter);
        return EntityMapper.toMeterResponse(meter);
    }

    @Transactional(readOnly = true)
    public List<MeterResponse> findByCustomer(UUID customerId) {
        assertStaffCustomerFilterAccess(customerId);
        customerService.getCustomer(customerId);
        return mapMeters(meterRepository.findByCustomerIdWithCustomer(customerId));
    }

    @Transactional
    public MeterResponse update(UUID id, MeterRequest request) {
        Meter meter = getMeter(id);
        if (!meter.getMeterNumber().equals(request.meterNumber().trim())
                && meterRepository.existsByMeterNumber(request.meterNumber().trim())) {
            throw new BadRequestException("Meter number already exists: " + request.meterNumber());
        }
        meter.setMeterNumber(request.meterNumber().trim());
        meter.setMeterType(request.meterType());
        meter.setInstallationDate(request.installationDate());
        if (request.status() != null) {
            meter.setStatus(request.status());
        }
        if (request.customerId() != null) {
            meter.setCustomer(resolveActiveCustomer(request.customerId()));
        }
        meterRepository.save(meter);
        return toResponse(id);
    }

    @Transactional
    public MeterResponse assignToCustomer(UUID meterId, AssignMeterRequest request) {
        Meter meter = getMeter(meterId);
        meter.setCustomer(resolveActiveCustomer(request.customerId()));
        meterRepository.save(meter);
        return toResponse(meterId);
    }

    private Customer resolveActiveCustomer(UUID customerId) {
        Customer customer = customerService.getCustomer(customerId);
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BadRequestException("Cannot assign meter to inactive customer");
        }
        return customer;
    }

    @Transactional
    public void delete(UUID id) {
        if (!meterReadingRepository.findByMeterId(id).isEmpty()) {
            throw new BadRequestException("Cannot delete meter with captured readings. Deactivate the meter instead.");
        }
        if (billRepository.existsByMeterId(id)) {
            throw new BadRequestException("Cannot delete meter with generated bills. Deactivate the meter instead.");
        }
        meterRepository.delete(getMeter(id));
    }

    @Transactional
    public MeterResponse updateStatus(UUID id, MeterStatus status) {
        Meter meter = getMeter(id);
        meter.setStatus(status);
        meterRepository.save(meter);
        return toResponse(id);
    }

    public Meter getMeter(UUID id) {
        return meterRepository.findByIdWithCustomer(id)
                .orElseThrow(() -> new NotFoundException("Meter not found with id: " + id));
    }

    private void assertCustomerMeterAccess(Meter meter) {
        AppUser user = securityUtils.currentUser();
        if (!hasRole(user, RoleName.ROLE_CUSTOMER)) {
            return;
        }
        Customer customer = resolveCurrentCustomer(user);
        if (meter.getCustomer() == null || !meter.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("You can only view your own meters");
        }
    }

    private void assertStaffCustomerFilterAccess(UUID customerId) {
        AppUser user = securityUtils.currentUser();
        if (hasRole(user, RoleName.ROLE_CUSTOMER)) {
            Customer customer = resolveCurrentCustomer(user);
            if (!customer.getId().equals(customerId)) {
                throw new ForbiddenException("You can only view your own meters");
            }
        }
    }

    private Customer resolveCurrentCustomer(AppUser user) {
        return customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Customer profile not found for current user"));
    }

    private boolean hasRole(AppUser user, RoleName roleName) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == roleName);
    }

    private void assertStaffOnly() {
        AppUser user = securityUtils.currentUser();
        if (hasRole(user, RoleName.ROLE_CUSTOMER)) {
            throw new ForbiddenException("Only Admin or Operator can list all meters");
        }
    }

    private MeterResponse toResponse(UUID meterId) {
        return meterRepository.findByIdWithCustomer(meterId)
                .map(EntityMapper::toMeterResponse)
                .orElseThrow(() -> new NotFoundException("Meter not found with id: " + meterId));
    }

    private List<MeterResponse> mapMeters(List<Meter> meters) {
        return meters.stream().map(EntityMapper::toMeterResponse).toList();
    }
}
