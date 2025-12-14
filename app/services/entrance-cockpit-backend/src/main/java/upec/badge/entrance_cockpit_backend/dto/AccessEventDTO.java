package upec.badge.entrance_cockpit_backend.dto;

public record AccessEventDTO(
        String badgeId,    
        String firstName,     
        String lastName,     
        String eventType,     
        String timestamp    
) {}
