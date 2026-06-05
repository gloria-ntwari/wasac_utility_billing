package gov.rw.javane.domain.entity;

import gov.rw.javane.domain.enums.MeterType;
import gov.rw.javane.domain.enums.TariffType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tariff_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private int version;

    @Enumerated(EnumType.STRING)
    @Column(name = "meter_type", nullable = false, length = 20)
    private MeterType meterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "tariff_type", nullable = false, length = 20)
    private TariffType tariffType;

    @Column(name = "flat_rate", precision = 14, scale = 2)
    private BigDecimal flatRate;

    @Column(name = "fixed_service_charge", nullable = false, precision = 14, scale = 2)
    private BigDecimal fixedServiceCharge;

    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatRate;

    @Column(name = "late_penalty_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal latePenaltyRate;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @OneToMany(mappedBy = "tariffVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TariffTier> tiers = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
