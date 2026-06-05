package gov.rw.javane.repository;

import gov.rw.javane.domain.entity.Customer;
import gov.rw.javane.domain.enums.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByNationalId(String nationalId);
    boolean existsByEmail(String email);
    Optional<Customer> findByNationalId(String nationalId);
    Optional<Customer> findByUserId(UUID userId);
    Optional<Customer> findByEmail(String email);
}
