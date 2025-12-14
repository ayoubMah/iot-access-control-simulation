package upec.badge.core_operational_backend.service;

import org.springframework.stereotype.Service;

import upec.badge.core_operational_backend.model.Person;

@Service
public class BadgeProcessingService {

    private final PersonService personService;
    private final MqttDecisionPublisher mqttPublisher;

    public BadgeProcessingService(
            PersonService personService,
            MqttDecisionPublisher mqttPublisher) {
        this.personService = personService;
        this.mqttPublisher = mqttPublisher;
    }

    public Person processBadgeScan(int badgeId) {
        Person person = personService.getPersonById(badgeId);

        if (person != null) {
            mqttPublisher.publishDoorBadgeOpenEvent(person);
            return person;
        }

        return null;
    }
}
