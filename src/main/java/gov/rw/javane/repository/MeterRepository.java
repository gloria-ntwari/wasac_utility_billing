package gov.rw.javane.repository;

import gov.rw.javane.domain.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeterRepository extends JpaRepository<Meter, UUID> {
    boolean existsByMeterNumber(String meterNumber);
    Optional<Meter> findByMeterNumber(String meterNumber);
    List<Meter> findByCustomerId(UUID customerId);

    @Query("SELECT m FROM Meter m LEFT JOIN FETCH m.customer")
    List<Meter> findAllWithCustomer();

    @Query("SELECT m FROM Meter m LEFT JOIN FETCH m.customer WHERE m.customer.id = :customerId")
    List<Meter> findByCustomerIdWithCustomer(@Param("customerId") UUID customerId);

    @Query("SELECT m FROM Meter m LEFT JOIN FETCH m.customer WHERE m.id = :id")
    Optional<Meter> findByIdWithCustomer(@Param("id") UUID id);
}
