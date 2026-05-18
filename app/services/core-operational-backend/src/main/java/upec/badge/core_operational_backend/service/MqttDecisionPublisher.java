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

import upec.badge.shared.model.Person;

@Service
public class MqttDecisionPublisher {

    private static final Logger log = LoggerFactory.getLogger(MqttDecisionPublisher.class);
    private final MessageChannel mqttOutboundChannel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MqttDecisionPublisher(MessageChannel mqttOutboundChannel) {
        this.mqttOutboundChannel = mqttOutboundChannel;
    }

    public void publishDoorBadgeOpenEvent(Person person) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("badgeId", person.getBadgeId());
        node.put("fullName", person.getFullName());
        node.put("status", "GRANTED");
        node.put("eventType", "badge");
        node.put("timestamp", Instant.now().toString());
        send(node, "badge", person.getBadgeId());
    }

    public void publishDoorManualEvent(String status) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("badgeId", "MANUAL");
        node.put("fullName", "Operator");
        node.put("status", status);
        node.put("eventType", "manual");
        node.put("timestamp", Instant.now().toString());
        send(node, "manual", "MANUAL");
    }

    private void send(ObjectNode node, String eventKind, String idForLog) {
        try {
            String message = Objects.requireNonNull(objectMapper.writeValueAsString(node));
            mqttOutboundChannel.send(MessageBuilder.withPayload(message).build());
            log.info("Sent MQTT {} event -> {}", eventKind, message);
        } catch (Exception e) {
            log.error("Failed to publish MQTT {} event for id={}", eventKind, idForLog, e);
        }
    }
}