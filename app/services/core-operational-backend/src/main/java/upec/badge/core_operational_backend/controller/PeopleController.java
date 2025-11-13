package upec.badge.core_operational_backend.controller;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upec.badge.core_operational_backend.model.RegisteredPerson;
import upec.badge.core_operational_backend.repository.RegisteredPersonRepository;
import upec.badge.core_operational_backend.service.EventProducer;
import upec.badge.core_operational_backend.service.MqttDecisionPublisher;

@RestController
@RequestMapping("/api/people")
public class PeopleController {

    private final RegisteredPersonRepository repository;
    private final EventProducer kafkaProducer;
    private final MqttDecisionPublisher mqttPublisher;

    public PeopleController(
            RegisteredPersonRepository repository,
            EventProducer kafkaProducer,
            MqttDecisionPublisher mqttPublisher
    ) {
        this.repository = repository;
        this.kafkaProducer = kafkaProducer;
        this.mqttPublisher = mqttPublisher;
    }

    /**
     * Processes a badge scan. This is the primary decision-making endpoint.
     * It is responsible for:
     * 1. Checking the database for the badge ID.
     * 2. Publishing the access attempt result (GRANTED/DENIED) to a Kafka topic for auditing.
     * 3. Publishing the open/close command to the MQTT broker for the IoT device.
     * 4. Returning the result of the database lookup.
     */
    @GetMapping("/{badgeId}")
    @Cacheable(value = "people", key = "#badgeId")
    public ResponseEntity<RegisteredPerson> processBadgeScan(@PathVariable String badgeId) {

        // Use the Optional to handle both found and not-found cases gracefully.
        return repository.findByBadgeId(badgeId)
                .map(person -> {
                    // --- Case: Person FOUND ---
                    boolean isAccessGranted = person.isActive();

                    // 1. Publish audit event to Kafka
                    kafkaProducer.publishBadgeEvent(badgeId, isAccessGranted);

                    // 2. Publish command to MQTT door lock
                    mqttPublisher.publishDecision(badgeId, isAccessGranted);

                    // 3. Return person data with a 200 OK status
                    return ResponseEntity.ok(person);
                })
                .orElseGet(() -> {
                    // --- Case: Person NOT FOUND ---
                    // For security, we still publish DENIED events for unknown badge scans.

                    // 1. Publish audit event to Kafka
                    kafkaProducer.publishBadgeEvent(badgeId, false);

                    // 2. Publish command to MQTT door lock
                    mqttPublisher.publishDecision(badgeId, false);

                    // 3. Return a 404 Not Found response, which is semantically correct.
                    return ResponseEntity.notFound().build();
                });
    }
}