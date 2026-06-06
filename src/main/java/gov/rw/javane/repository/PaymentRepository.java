package gov.rw.javane.repository;

import gov.rw.javane.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByBillId(UUID billId);
    List<Payment> findByBillCustomerId(UUID customerId);

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p WHERE p.bill.id = :billId")
    BigDecimal sumAmountPaidByBillId(@Param("billId") UUID billId);
}
