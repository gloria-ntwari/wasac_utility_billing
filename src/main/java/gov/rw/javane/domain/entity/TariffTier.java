package gov.rw.javane.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tariff_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffTier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tariff_version_id", nullable = false)
    private TariffVersion tariffVersion;

    @Column(name = "from_units", nullable = false, precision = 14, scale = 2)
    private BigDecimal fromUnits;

    @Column(name = "to_units", precision = 14, scale = 2)
    private BigDecimal toUnits;

    @Column(name = "rate_per_unit", nullable = false, precision = 14, scale = 2)
    private BigDecimal ratePerUnit;
}
