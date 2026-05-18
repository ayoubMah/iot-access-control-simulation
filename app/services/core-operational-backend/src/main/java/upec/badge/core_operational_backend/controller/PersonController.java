package upec.badge.core_operational_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import upec.badge.shared.model.Person;
import upec.badge.core_operational_backend.service.PersonService;

@RestController
@RequestMapping("/api/people")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/{badgeId}")
    public ResponseEntity<Person> getPersonByBadgeId(@PathVariable String badgeId) {
        return personService.getPersonByBadgeId(badgeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
