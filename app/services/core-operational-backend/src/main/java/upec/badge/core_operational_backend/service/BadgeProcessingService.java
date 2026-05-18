package upec.badge.core_operational_backend.service;

import org.springframework.stereotype.Service;

import upec.badge.shared.model.Person;

import java.util.Optional;

@Service
public class BadgeProcessingService {

    private final PersonService personService;
    private final MqttDecisionPublisher mqttPublisher;
    private final EventProducer eventProducer;

    public BadgeProcessingService(
            PersonService personService,
            MqttDecisionPublisher mqttPublisher,
            EventProducer eventProducer) {
        this.personService = personService;
        this.mqttPublisher = mqttPublisher;
        this.eventProducer = eventProducer;
    }

    public Optional<Person> processBadgeScan(String badgeId) {
        Optional<Person> result = personService.getPersonByBadgeId(badgeId);

        if (result.isPresent()) {
            Person person = result.get();
            boolean granted = person.isActive();
            if (granted) {
                mqttPublisher.publishDoorBadgeOpenEvent(person);
            }
            eventProducer.publishBadgeEvent(badgeId, granted, person.getFullName());
        } else {
            eventProducer.publishBadgeEvent(badgeId, false, null);
        }

        return result;
    }
}
