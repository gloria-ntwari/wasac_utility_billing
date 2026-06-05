package gov.rw.javane.service;

import gov.rw.javane.common.exception.BadRequestException;
import gov.rw.javane.common.exception.NotFoundException;
import gov.rw.javane.domain.entity.AppUser;
import gov.rw.javane.domain.entity.Customer;
import gov.rw.javane.domain.enums.CustomerStatus;
import gov.rw.javane.dto.customer.CustomerRequest;
import gov.rw.javane.dto.customer.CustomerResponse;
import gov.rw.javane.mapper.EntityMapper;
import gov.rw.javane.repository.AppUserRepository;
import gov.rw.javane.repository.BillRepository;
import gov.rw.javane.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AppUserRepository appUserRepository;
    private final BillRepository billRepository;

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        validateUnique(request, null);
        Customer customer = Customer.builder()
                .fullName(request.fullName().trim())
                .nationalId(request.nationalId().trim())
                .email(request.email().toLowerCase().trim())
                .phoneNumber(request.phoneNumber().trim())
                .address(request.address().trim())
                .status(request.status() != null ? request.status() : CustomerStatus.ACTIVE)
                .build();
        return EntityMapper.toCustomerResponse(customerRepository.save(customer));
    }

    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream().map(EntityMapper::toCustomerResponse).toList();
    }

    public CustomerResponse findById(UUID id) {
        return EntityMapper.toCustomerResponse(getCustomer(id));
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = getCustomer(id);
        validateUnique(request, id);

        customer.setFullName(request.fullName().trim());
        customer.setNationalId(request.nationalId().trim());
        customer.setEmail(request.email().toLowerCase().trim());
        customer.setPhoneNumber(request.phoneNumber().trim());
        customer.setAddress(request.address().trim());
        if (request.status() != null) {
            customer.setStatus(request.status());
        }
        return EntityMapper.toCustomerResponse(customerRepository.save(customer));
    }

    @Transactional
    public void delete(UUID id) {
        Customer customer = getCustomer(id);
        if (!billRepository.findByCustomerId(id).isEmpty()) {
            throw new BadRequestException("Cannot delete customer with existing bills. Deactivate the customer instead.");
        }
        detachLinkedUser(customer);
        customerRepository.delete(customer);
    }

    private void detachLinkedUser(Customer customer) {
        AppUser user = customer.getUser();
        if (user != null) {
            user.setCustomer(null);
            customer.setUser(null);
            appUserRepository.saveAndFlush(user);
        }
    }

    @Transactional
    public CustomerResponse updateStatus(UUID id, CustomerStatus status) {
        Customer customer = getCustomer(id);
        customer.setStatus(status);
        return EntityMapper.toCustomerResponse(customerRepository.save(customer));
    }

    public Customer getCustomer(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + id));
    }

    private void validateUnique(CustomerRequest request, UUID excludeId) {
        customerRepository.findByNationalId(request.nationalId().trim())
                .filter(c -> excludeId == null || !c.getId().equals(excludeId))
                .ifPresent(c -> {
                    throw new BadRequestException("Customer with this National ID already exists");
                });
        customerRepository.findByEmail(request.email().toLowerCase().trim())
                .filter(c -> excludeId == null || !c.getId().equals(excludeId))
                .ifPresent(c -> {
                    throw new BadRequestException("Customer email is already registered: " + request.email());
                });
    }
}
