package upec.badge.entrance_cockpit_backend.dto;

public record AccessEventDTO(
        String badgeId,
        String fullName,
        String status,
        String eventType,
        String timestamp
) {}
