package upec.badge.core_operational_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Service
public class MqttDecisionPublisher {

    private static final Logger log = LoggerFactory.getLogger(MqttDecisionPublisher.class);
    private final MessageChannel mqttOutboundChannel;

    public MqttDecisionPublisher(MessageChannel mqttOutboundChannel) {
        this.mqttOutboundChannel = mqttOutboundChannel;
    }

    public void publishDecision(String badgeId, boolean granted) {
        String status = granted ? "GRANTED" : "DENIED";
        String message = String.format(
                "{\"badge_id\":\"%s\",\"status\":\"%s\"}",
                badgeId, status
        );
        mqttOutboundChannel.send(MessageBuilder.withPayload(message).build());
        log.info("Sent MQTT decision -> {}", message);
    }
}