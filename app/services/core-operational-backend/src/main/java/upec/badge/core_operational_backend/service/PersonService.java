package upec.badge.core_operational_backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import upec.badge.core_operational_backend.model.Person;
import upec.badge.core_operational_backend.repository.PersonRepository;

@Service
public class PersonService {

    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    public Optional<Person> getPersonByBadgeId(String badgeId) {
        return repository.findByBadgeId(badgeId);
    }
}
