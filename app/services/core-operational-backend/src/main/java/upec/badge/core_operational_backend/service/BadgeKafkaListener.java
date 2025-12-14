package upec.badge.core_operational_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BadgeKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(BadgeKafkaListener.class);

    private final BadgeProcessingService badgeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BadgeKafkaListener(BadgeProcessingService badgeService) {
        this.badgeService = badgeService;
    }

    @KafkaListener(
        topics = "entrance_attempts",
        groupId = "core-operational-backend"
    )
    public void onBadgeEvent(String message) {

        try {
            JsonNode json = objectMapper.readTree(message);
            int badgeId = json.get("badgeId").asInt();

            log.info("Received badge event for badgeId={}", badgeId);

            badgeService.processBadgeScan(badgeId);

        } catch (Exception e) {
            log.error("Failed to process badge event: {}", message, e);
            // optional: send to DLQ
        }
    }
}
