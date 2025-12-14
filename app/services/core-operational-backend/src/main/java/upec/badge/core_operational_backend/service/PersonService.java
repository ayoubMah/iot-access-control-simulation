package upec.badge.core_operational_backend.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import upec.badge.core_operational_backend.model.Person;
import upec.badge.core_operational_backend.repository.PersonRepository;

@Service
public class PersonService {

    @Autowired
    private PersonRepository repository;

    public Person getPersonById(int badgeId) {
        Optional<Person> person = repository.findByBadgeId(badgeId);

        if (person.isPresent()) return person.get();

        return null;
    }
    
    public boolean authenticatePerson(Person person) {
        return person != null;
    }
}
