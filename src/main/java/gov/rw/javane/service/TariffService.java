package gov.rw.javane.service;

import gov.rw.javane.common.exception.BadRequestException;
import gov.rw.javane.common.exception.NotFoundException;
import gov.rw.javane.domain.entity.TariffTier;
import gov.rw.javane.domain.entity.TariffVersion;
import gov.rw.javane.domain.enums.MeterType;
import gov.rw.javane.domain.enums.TariffType;
import gov.rw.javane.dto.tariff.TariffRequest;
import gov.rw.javane.dto.tariff.TariffResponse;
import gov.rw.javane.dto.tariff.TariffTierRequest;
import gov.rw.javane.dto.tariff.TariffTierResponse;
import gov.rw.javane.mapper.EntityMapper;
import gov.rw.javane.repository.TariffVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TariffService {

    private final TariffVersionRepository tariffVersionRepository;

    @Transactional
    public TariffResponse create(TariffRequest request) {
        validateTariffRequest(request);

        int nextVersion = tariffVersionRepository.findTopByMeterTypeOrderByVersionDesc(request.meterType())
                .map(t -> t.getVersion() + 1)
                .orElse(1);

        closePreviousVersion(request.meterType(), request.effectiveFrom());

        TariffVersion tariff = TariffVersion.builder()
                .version(nextVersion)
                .meterType(request.meterType())
                .tariffType(request.tariffType())
                .flatRate(request.flatRate())
                .fixedServiceCharge(request.fixedServiceCharge())
                .vatRate(request.vatRate())
                .latePenaltyRate(request.latePenaltyRate())
                .effectiveFrom(request.effectiveFrom())
                .build();

        if (request.tariffType() == TariffType.TIER && request.tiers() != null) {
            for (TariffTierRequest tierReq : request.tiers()) {
                TariffTier tier = TariffTier.builder()
                        .tariffVersion(tariff)
                        .fromUnits(tierReq.fromUnits())
                        .toUnits(tierReq.toUnits())
                        .ratePerUnit(tierReq.ratePerUnit())
                        .build();
                tariff.getTiers().add(tier);
            }
        }

        TariffVersion saved = tariffVersionRepository.save(tariff);
        return EntityMapper.toTariffResponse(getTariffWithTiers(saved.getId()));
    }

    @Transactional(readOnly = true)
    public List<TariffResponse> findAll() {
        return mapTariffs(tariffVersionRepository.findAllWithTiers());
    }

    @Transactional(readOnly = true)
    public TariffResponse findById(UUID id) {
        return EntityMapper.toTariffResponse(getTariffWithTiers(id));
    }

    @Transactional(readOnly = true)
    public List<TariffResponse> findByMeterType(MeterType meterType) {
        return mapTariffs(tariffVersionRepository.findByMeterTypeWithTiers(meterType));
    }

    @Transactional
    public TariffResponse update(UUID id, TariffRequest request) {
        throw new BadRequestException("Tariffs are versioned. Create a new tariff version instead of updating an existing one.");
    }

    @Transactional
    public void delete(UUID id) {
        throw new BadRequestException("Tariff versions cannot be deleted to preserve billing history.");
    }

    /**
     * Resolves the tariff version active for a meter type on the first day of the billing month.
     * Versioned tariffs: if Tariff A starts 2026-01-01 and Tariff B starts 2026-07-01,
     * a June 2026 bill uses Tariff A; July 2026 and later use Tariff B.
     */
    @Transactional(readOnly = true)
    public TariffVersion resolveApplicableTariff(MeterType meterType, LocalDate billingPeriodStart) {
        List<TariffVersion> applicable = tariffVersionRepository.findApplicableTariffsWithTiers(
                meterType, billingPeriodStart);
        if (applicable.isEmpty()) {
            throw new BadRequestException(
                    "No " + meterType + " tariff configured for billing period starting "
                            + billingPeriodStart + ". Create a tariff version with effectiveFrom on or before that date.");
        }
        return applicable.get(0);
    }

    @Transactional
    public TariffResponse addTier(UUID tariffId, TariffTierRequest tierRequest) {
        TariffVersion tariff = getTariff(tariffId);
        if (tariff.getTariffType() != TariffType.TIER) {
            throw new BadRequestException("Tiers can only be added to TIER tariff types");
        }
        TariffTier tier = TariffTier.builder()
                .tariffVersion(tariff)
                .fromUnits(tierRequest.fromUnits())
                .toUnits(tierRequest.toUnits())
                .ratePerUnit(tierRequest.ratePerUnit())
                .build();
        tariff.getTiers().add(tier);
        return EntityMapper.toTariffResponse(tariffVersionRepository.save(tariff));
    }

    @Transactional(readOnly = true)
    public List<TariffTierResponse> getTiers(UUID tariffId) {
        TariffVersion tariff = getTariffWithTiers(tariffId);
        return tariff.getTiers().stream()
                .map(t -> new TariffTierResponse(t.getId(), t.getFromUnits(), t.getToUnits(), t.getRatePerUnit()))
                .toList();
    }

    private void closePreviousVersion(MeterType meterType, LocalDate effectiveFrom) {
        tariffVersionRepository.findTopByMeterTypeOrderByVersionDesc(meterType)
                .filter(t -> t.getEffectiveTo() == null)
                .ifPresent(previous -> {
                    if (!effectiveFrom.isAfter(previous.getEffectiveFrom())) {
                        throw new BadRequestException("New tariff effective date must be after the current tariff effective date ("
                                + previous.getEffectiveFrom() + ")");
                    }
                    previous.setEffectiveTo(effectiveFrom.minusDays(1));
                    tariffVersionRepository.save(previous);
                });
    }

    private void validateTariffRequest(TariffRequest request) {
        if (request.tariffType() == TariffType.FLAT && request.flatRate() == null) {
            throw new BadRequestException("Flat rate is required for FLAT tariff type");
        }
        if (request.tariffType() == TariffType.TIER) {
            if (request.flatRate() != null) {
                throw new BadRequestException("flatRate must be null for TIER tariffs");
            }
            if (request.tiers() == null || request.tiers().isEmpty()) {
                throw new BadRequestException("At least one tier is required for TIER tariff type");
            }
        }
    }

    private TariffVersion getTariff(UUID id) {
        return tariffVersionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tariff not found with id: " + id));
    }

    private TariffVersion getTariffWithTiers(UUID id) {
        return tariffVersionRepository.findByIdWithTiers(id)
                .orElseThrow(() -> new NotFoundException("Tariff not found with id: " + id));
    }

    private List<TariffResponse> mapTariffs(List<TariffVersion> tariffs) {
        return tariffs.stream().map(EntityMapper::toTariffResponse).toList();
    }
}
