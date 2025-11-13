package upec.badge.entrance_cockpit_backend.dto;

import java.time.LocalDateTime;

// Using a record for an immutable data carrier.
public record AccessEventDTO(
        String badgeId,
        String personName,
        boolean accessGranted,
        LocalDateTime timestamp
) {}