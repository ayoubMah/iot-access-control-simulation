package upec.badge.core_operational_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upec.badge.core_operational_backend.service.EventProducer;
import upec.badge.core_operational_backend.service.MqttDecisionPublisher;

@RestController
@RequestMapping("/api/core/manual")
public class ManualControlController {

    private final EventProducer kafkaProducer;
    private final MqttDecisionPublisher mqttPublisher;

    public ManualControlController(EventProducer kafkaProducer,
                                   MqttDecisionPublisher mqttPublisher) {
        this.kafkaProducer = kafkaProducer;
        this.mqttPublisher = mqttPublisher;
    }

    @PostMapping("/open")
    public ResponseEntity<Void> openDoor() {
        boolean isAccessGranted = true;

        // Publish to Kafka
        kafkaProducer.publishBadgeEvent("MANUAL-OPEN", isAccessGranted);

        // Publish to MQTT
        mqttPublisher.publishDecision("MANUAL-OPEN", isAccessGranted);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/close")
    public ResponseEntity<Void> closeDoor() {
        boolean isAccessGranted = false;

        kafkaProducer.publishBadgeEvent("MANUAL-CLOSE", isAccessGranted);
        mqttPublisher.publishDecision("MANUAL-CLOSE", isAccessGranted);

        return ResponseEntity.ok().build();
    }
}
