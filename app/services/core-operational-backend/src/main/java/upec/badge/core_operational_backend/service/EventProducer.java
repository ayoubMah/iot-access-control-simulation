package upec.badge.core_operational_backend.service;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventProducer {

    private static final Logger log = LoggerFactory.getLogger(EventProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String accessEventsTopic;

    public EventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${kafka.topic.access-events}") String accessEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.accessEventsTopic = accessEventsTopic;
    }

    public void publishBadgeEvent(String badgeId, boolean granted, String fullName) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("badgeId", badgeId);
            node.put("fullName", fullName != null ? fullName : "Unknown");
            node.put("status", granted ? "GRANTED" : "DENIED");
            node.put("eventType", "badge");
            node.put("timestamp", Instant.now().toString());

            String message = objectMapper.writeValueAsString(node);
            kafkaTemplate.send(accessEventsTopic, badgeId, message);
            log.info("Sent badge event to Kafka -> {}", message);
        } catch (Exception e) {
            log.error("Failed to publish badge event for badgeId={}", badgeId, e);
        }
    }

    public void publishManualEvent(String action) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("badgeId", "MANUAL");
            node.put("fullName", "Operator");
            node.put("status", action);
            node.put("eventType", "manual");
            node.put("timestamp", Instant.now().toString());

            String message = objectMapper.writeValueAsString(node);
            kafkaTemplate.send(accessEventsTopic, "MANUAL", message);
            log.info("Sent manual event to Kafka -> {}", message);
        } catch (Exception e) {
            log.error("Failed to publish manual event", e);
        }
    }
}