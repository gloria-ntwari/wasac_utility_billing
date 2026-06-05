package gov.rw.javane.repository;

import gov.rw.javane.domain.entity.Bill;
import gov.rw.javane.domain.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {
    List<Bill> findByCustomerId(UUID customerId);
    boolean existsByMeterId(UUID meterId);
    List<Bill> findByStatus(BillStatus status);
    Optional<Bill> findByReadingId(UUID readingId);

    @Query("""
            SELECT DISTINCT b FROM Bill b
            LEFT JOIN FETCH b.customer
            LEFT JOIN FETCH b.meter
            LEFT JOIN FETCH b.reading
            LEFT JOIN FETCH b.tariffVersion
            LEFT JOIN FETCH b.payments
            LEFT JOIN FETCH b.approvedBy
            ORDER BY b.createdAt DESC
            """)
    List<Bill> findAllWithDetails();

    @Query("""
            SELECT DISTINCT b FROM Bill b
            LEFT JOIN FETCH b.customer
            LEFT JOIN FETCH b.meter
            LEFT JOIN FETCH b.reading
            LEFT JOIN FETCH b.tariffVersion
            LEFT JOIN FETCH b.payments
            LEFT JOIN FETCH b.approvedBy
            WHERE b.customer.id = :customerId
            ORDER BY b.createdAt DESC
            """)
    List<Bill> findByCustomerIdWithDetails(@Param("customerId") UUID customerId);

    @Query("""
            SELECT DISTINCT b FROM Bill b
            LEFT JOIN FETCH b.customer
            LEFT JOIN FETCH b.meter
            LEFT JOIN FETCH b.reading
            LEFT JOIN FETCH b.tariffVersion
            LEFT JOIN FETCH b.payments
            LEFT JOIN FETCH b.approvedBy
            WHERE b.id = :id
            """)
    Optional<Bill> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT b FROM Bill b
            LEFT JOIN FETCH b.customer
            LEFT JOIN FETCH b.meter
            LEFT JOIN FETCH b.reading
            LEFT JOIN FETCH b.tariffVersion
            LEFT JOIN FETCH b.payments
            LEFT JOIN FETCH b.approvedBy
            WHERE b.status = :status
            ORDER BY b.createdAt DESC
            """)
    List<Bill> findByStatusWithDetails(@Param("status") BillStatus status);
}
