package upec.badge.core_operational_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upec.badge.core_operational_backend.service.MqttDecisionPublisher;

@RestController
@RequestMapping("/api/manual")
public class ManualController {

    private final MqttDecisionPublisher mqttDecisionPublisher;
    private final EventStreamController sseController;


    public ManualController(MqttDecisionPublisher mqttDecisionPublisher, EventStreamController sseController) {
        this.mqttDecisionPublisher = mqttDecisionPublisher;
        this.sseController = sseController;
    }

    @PostMapping("/{action}")
    public ResponseEntity<String> manualAction(@PathVariable String action) {
        boolean granted = action.equalsIgnoreCase("open");
        mqttDecisionPublisher.publishDecision("MANUAL", granted);
        sseController.broadcast(new PeopleController.DoorEvent("Manual Action", granted ? "open" : "closed"));
        return ResponseEntity.ok("Door " + (granted ? "opened" : "closed"));
    }
}