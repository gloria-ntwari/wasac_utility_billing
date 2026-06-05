package gov.rw.javane.repository;

import gov.rw.javane.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByBillId(UUID billId);
    List<Payment> findByBillCustomerId(UUID customerId);
}
