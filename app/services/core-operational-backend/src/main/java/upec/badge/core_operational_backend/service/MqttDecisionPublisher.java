package upec.badge.core_operational_backend.service;

import java.time.Instant;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import upec.badge.core_operational_backend.model.Person;

@Service
public class MqttDecisionPublisher {

    private static final Logger log = LoggerFactory.getLogger(MqttDecisionPublisher.class);
    private final MessageChannel mqttOutboundChannel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MqttDecisionPublisher(MessageChannel mqttOutboundChannel) {
        this.mqttOutboundChannel = mqttOutboundChannel;
    }

    public void publishDoorBadgeOpenEvent(Person person) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("badgeId", person.getBadgeId());
            node.put("fullName", person.getFullName());
            node.put("eventType", "badge");
            node.put("timestamp", Instant.now().toString());

            String message = Objects.requireNonNull(objectMapper.writeValueAsString(node));
            mqttOutboundChannel.send(MessageBuilder.withPayload(message).build());
            log.info("Sent MQTT badge event -> {}", message);
        } catch (Exception e) {
            log.error("Failed to publish MQTT badge event for badgeId={}", person.getBadgeId(), e);
        }
    }

    public void publishDoorManualOpenEvent() {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("eventType", "manual");
            node.put("timestamp", Instant.now().toString());

            String message = Objects.requireNonNull(objectMapper.writeValueAsString(node));
            mqttOutboundChannel.send(MessageBuilder.withPayload(message).build());
            log.info("Sent MQTT manual open event -> {}", message);
        } catch (Exception e) {
            log.error("Failed to publish MQTT manual event", e);
        }
    }
}