package gov.rw.javane.service;

import gov.rw.javane.common.exception.BadRequestException;
import gov.rw.javane.common.exception.NotFoundException;
import gov.rw.javane.domain.entity.*;
import gov.rw.javane.domain.enums.CustomerStatus;
import gov.rw.javane.domain.enums.MeterStatus;
import gov.rw.javane.dto.reading.ReadingRequest;
import gov.rw.javane.dto.reading.ReadingResponse;
import gov.rw.javane.mapper.EntityMapper;
import gov.rw.javane.repository.BillRepository;
import gov.rw.javane.repository.MeterReadingRepository;
import gov.rw.javane.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final MeterService meterService;
    private final BillRepository billRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public ReadingResponse capture(ReadingRequest request) {
        Meter meter = meterService.getMeter(request.meterId());

        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new BadRequestException("Cannot capture reading on an inactive meter");
        }
        if (meter.getCustomer() == null) {
            throw new BadRequestException("Meter is not assigned to a customer");
        }
        Customer customer = meter.getCustomer();
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BadRequestException("Inactive customers cannot receive bills. Customer '" + customer.getFullName() + "' is inactive.");
        }
        if (request.currentReading().compareTo(request.previousReading()) <= 0) {
            throw new BadRequestException("Current reading must be greater than previous reading");
        }
        if (request.readingDate().isBefore(meter.getInstallationDate())) {
            throw new BadRequestException("Reading date (" + request.readingDate()
                    + ") cannot be before meter installation date (" + meter.getInstallationDate() + ")");
        }

        int billingMonth = request.readingDate().getMonthValue();
        int billingYear = request.readingDate().getYear();

        if (meterReadingRepository.existsByMeterIdAndBillingMonthAndBillingYear(meter.getId(), billingMonth, billingYear)) {
            throw new gov.rw.javane.common.exception.ConflictException(
                    "A reading already exists for this meter in "
                            + String.format("%02d/%d", billingMonth, billingYear));
        }

        meterReadingRepository.findTopByMeterIdOrderByReadingDateDescCreatedAtDesc(meter.getId())
                .ifPresent(last -> {
                    if (request.previousReading().compareTo(last.getCurrentReading()) != 0) {
                        throw new BadRequestException("Previous reading must match the last recorded current reading ("
                                + last.getCurrentReading() + ")");
                    }
                });

        AppUser operator = securityUtils.currentUser();

        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .previousReading(request.previousReading())
                .currentReading(request.currentReading())
                .readingDate(request.readingDate())
                .billingMonth(billingMonth)
                .billingYear(billingYear)
                .capturedBy(operator)
                .build();
        reading = meterReadingRepository.save(reading);
        MeterReading saved = meterReadingRepository.findByIdWithMeter(reading.getId())
                .orElse(reading);
        return EntityMapper.toReadingResponse(saved, null);
    }

    @Transactional(readOnly = true)
    public List<ReadingResponse> findAll(UUID meterId) {
        List<MeterReading> readings = meterId != null
                ? meterReadingRepository.findByMeterIdWithMeter(meterId)
                : meterReadingRepository.findAllWithMeter();
        return mapReadings(readings);
    }

    @Transactional(readOnly = true)
    public ReadingResponse findById(UUID id) {
        MeterReading reading = meterReadingRepository.findByIdWithMeter(id)
                .orElseThrow(() -> new NotFoundException("Reading not found with id: " + id));
        Bill bill = billRepository.findByReadingId(reading.getId()).orElse(null);
        return EntityMapper.toReadingResponse(reading, bill);
    }

    private List<ReadingResponse> mapReadings(List<MeterReading> readings) {
        return readings.stream()
                .map(r -> EntityMapper.toReadingResponse(r, billRepository.findByReadingId(r.getId()).orElse(null)))
                .toList();
    }

    @Transactional
    public ReadingResponse update(UUID id, ReadingRequest request) {
        throw new BadRequestException("Meter readings cannot be updated after capture to preserve billing integrity.");
    }

    @Transactional
    public void delete(UUID id) {
        throw new BadRequestException("Meter readings cannot be deleted after capture to preserve billing integrity.");
    }
}
