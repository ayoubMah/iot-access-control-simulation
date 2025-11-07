package upec.badge.core_operational_backend.controller;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upec.badge.core_operational_backend.model.RegisteredPerson;
import upec.badge.core_operational_backend.repository.RegisteredPersonRepository;
import upec.badge.core_operational_backend.service.EventProducer;
import upec.badge.core_operational_backend.service.MqttDecisionPublisher; // Added import

import java.util.List;

@RestController
@RequestMapping("/api/people")
public class PeopleController {

    private final RegisteredPersonRepository repository;
    private final EventProducer producer;
    private final MqttDecisionPublisher mqttDecisionPublisher; // Added field

    public PeopleController(RegisteredPersonRepository repository, EventProducer producer,
                            MqttDecisionPublisher mqttDecisionPublisher) { // Modified constructor
        this.repository = repository;
        this.producer = producer;
        this.mqttDecisionPublisher = mqttDecisionPublisher; // Initialized field
    }

    //@GetMapping
    //public List<RegisteredPerson> all() {
     //   return repository.findAll();
    //}


    @GetMapping("/{badgeId}")
    @Cacheable(value = "people", key = "#badgeId")
    public RegisteredPerson findByBadge(@PathVariable String badgeId) {
        RegisteredPerson person = repository.findByBadgeId(badgeId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        boolean granted = person.isActive();
        producer.publishBadgeEvent(badgeId, granted);
        mqttDecisionPublisher.publishDecision(badgeId, granted); // Added MQTT publish
        return person;
    }

}
