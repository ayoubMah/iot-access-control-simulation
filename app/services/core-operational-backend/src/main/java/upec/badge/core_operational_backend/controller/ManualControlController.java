package upec.badge.core_operational_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import upec.badge.core_operational_backend.service.EventProducer;
import upec.badge.core_operational_backend.service.MqttDecisionPublisher;

@RestController
@RequestMapping("/api/core/manual")
public class ManualControlController {

    private final MqttDecisionPublisher mqttPublisher;

    public ManualControlController(EventProducer kafkaProducer,
                                   MqttDecisionPublisher mqttPublisher) {
        this.mqttPublisher = mqttPublisher;
    }

    @PostMapping("/open")
    public ResponseEntity<Void> openDoor() {
        
        // Publish to MQTT
        mqttPublisher.publishDoorManualOpenEvent();

        return ResponseEntity.ok().build();
    }
}
