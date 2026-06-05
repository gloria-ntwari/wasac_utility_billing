package gov.rw.javane.repository;

import gov.rw.javane.domain.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {
    boolean existsByMeterIdAndBillingMonthAndBillingYear(UUID meterId, int month, int year);
    Optional<MeterReading> findTopByMeterIdOrderByReadingDateDescCreatedAtDesc(UUID meterId);
    List<MeterReading> findByMeterId(UUID meterId);

    @Query("""
            SELECT r FROM MeterReading r
            JOIN FETCH r.meter m
            LEFT JOIN FETCH m.customer
            ORDER BY r.readingDate DESC, r.createdAt DESC
            """)
    List<MeterReading> findAllWithMeter();

    @Query("""
            SELECT r FROM MeterReading r
            JOIN FETCH r.meter m
            LEFT JOIN FETCH m.customer
            WHERE m.id = :meterId
            ORDER BY r.readingDate DESC, r.createdAt DESC
            """)
    List<MeterReading> findByMeterIdWithMeter(@Param("meterId") UUID meterId);

    @Query("""
            SELECT r FROM MeterReading r
            JOIN FETCH r.meter m
            LEFT JOIN FETCH m.customer
            WHERE r.id = :id
            """)
    Optional<MeterReading> findByIdWithMeter(@Param("id") UUID id);
}
