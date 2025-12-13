package upec.badge.core_operational_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class EventProducer {

    private static final Logger log = LoggerFactory.getLogger(EventProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public EventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishBadgeEvent(String badgeId, boolean granted) {
        String topic = "entrance_attempts";
        String status = granted ? "GRANTED" : "DENIED";
        String message = String.format(
                "{\"badge_id\":\"%s\",\"status\":\"%s\",\"timestamp\":\"%s\"}",
                badgeId, status, Instant.now().toString()
        );
        kafkaTemplate.send(topic, badgeId, message);
        log.info("Sent event to Kafka -> {}", message);
    }
}