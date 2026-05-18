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
    private final EventProducer eventProducer;

    public ManualControlController(MqttDecisionPublisher mqttPublisher,
                                   EventProducer eventProducer) {
        this.mqttPublisher = mqttPublisher;
        this.eventProducer = eventProducer;
    }

    @PostMapping("/open")
    public ResponseEntity<Void> openDoor() {
        mqttPublisher.publishDoorManualEvent("OPEN");
        eventProducer.publishManualEvent("OPEN");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/close")
    public ResponseEntity<Void> closeDoor() {
        mqttPublisher.publishDoorManualEvent("CLOSE");
        eventProducer.publishManualEvent("CLOSE");
        return ResponseEntity.ok().build();
    }
}
