package gov.rw.javane.dto.notification;

import gov.rw.javane.domain.enums.NotificationChannel;
import gov.rw.javane.domain.enums.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID customerId,
        UUID billId,
        NotificationChannel channel,
        NotificationStatus status,
        String message,
        Instant createdAt
) {}
