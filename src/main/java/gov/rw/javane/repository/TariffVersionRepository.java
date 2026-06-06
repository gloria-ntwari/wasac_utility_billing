package gov.rw.javane.repository;

import gov.rw.javane.domain.entity.TariffVersion;
import gov.rw.javane.domain.enums.MeterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TariffVersionRepository extends JpaRepository<TariffVersion, UUID> {
    List<TariffVersion> findByMeterTypeOrderByVersionDesc(MeterType meterType);

    @Query("""
            SELECT DISTINCT t FROM TariffVersion t
            LEFT JOIN FETCH t.tiers
            WHERE t.id = :id
            """)
    Optional<TariffVersion> findByIdWithTiers(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT t FROM TariffVersion t
            LEFT JOIN FETCH t.tiers
            ORDER BY t.meterType, t.version DESC
            """)
    List<TariffVersion> findAllWithTiers();

    @Query("""
            SELECT DISTINCT t FROM TariffVersion t
            LEFT JOIN FETCH t.tiers
            WHERE t.meterType = :meterType
            ORDER BY t.version DESC
            """)
    List<TariffVersion> findByMeterTypeWithTiers(@Param("meterType") MeterType meterType);

    @Query("""
            SELECT t FROM TariffVersion t
            WHERE t.meterType = :meterType
              AND t.effectiveFrom <= :billingDate
              AND (t.effectiveTo IS NULL OR t.effectiveTo >= :billingDate)
            ORDER BY t.effectiveFrom DESC, t.version DESC
            """)
    List<TariffVersion> findApplicableTariffs(@Param("meterType") MeterType meterType,
                                              @Param("billingDate") LocalDate billingDate);

    @Query("""
            SELECT DISTINCT t FROM TariffVersion t
            LEFT JOIN FETCH t.tiers
            WHERE t.meterType = :meterType
              AND t.effectiveFrom <= :billingDate
              AND (t.effectiveTo IS NULL OR t.effectiveTo >= :billingDate)
            ORDER BY t.effectiveFrom DESC, t.version DESC
            """)
    List<TariffVersion> findApplicableTariffsWithTiers(@Param("meterType") MeterType meterType,
                                                     @Param("billingDate") LocalDate billingDate);

    Optional<TariffVersion> findTopByMeterTypeOrderByVersionDesc(MeterType meterType);
}
