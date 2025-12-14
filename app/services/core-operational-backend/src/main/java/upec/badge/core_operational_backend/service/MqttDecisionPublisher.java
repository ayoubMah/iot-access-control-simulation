package upec.badge.core_operational_backend.service;

import java.time.LocalDateTime;

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

    public MqttDecisionPublisher(MessageChannel mqttOutboundChannel) {
        this.mqttOutboundChannel = mqttOutboundChannel;
    }

    public void publishDoorBadgeOpenEvent(Person person) {
        String message = String.format(
                "{\"badgeId\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"eventType\":\"%s\",\"timestamp\":\"%s\"}",
                person.getBadgeId(),
                person.getFirstName(),
                person.getLastName(),
                "badge",
                LocalDateTime.now().toString());

        mqttOutboundChannel.send(MessageBuilder.withPayload(message).build());
        log.info("Sent MQTT badge event -> {}", message);
    }

    public void publishDoorManualOpenEvent() {
        String message = String.format(
                "{\"eventType\":\"%s\"}",
                "manual");

        mqttOutboundChannel.send(MessageBuilder.withPayload(message).build());
        log.info("Sent MQTT manual open event -> {}", message);
    }

}