package upec.badge.core_operational_backend.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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
                "{\"badgeId\":\"%s\",\"status\":\"%s\",\"timestamp\":\"%s\"}",
                badgeId, status, Instant.now().toString()
        );
        kafkaTemplate.send(topic, badgeId, message);
        log.info("Sent event to Kafka -> {}", message);
    }
}