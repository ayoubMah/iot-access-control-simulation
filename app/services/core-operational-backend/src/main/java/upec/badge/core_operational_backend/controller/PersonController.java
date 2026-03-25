package upec.badge.core_operational_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import upec.badge.core_operational_backend.model.Person;
import upec.badge.core_operational_backend.service.BadgeProcessingService;

@RestController
@RequestMapping("/api/people")
public class PersonController {

    private final BadgeProcessingService badgeProcessingService;

    public PersonController(BadgeProcessingService badgeProcessingService) {
        this.badgeProcessingService = badgeProcessingService;
    }

    @GetMapping("/{badgeId}")
    public ResponseEntity<Person> scanBadge(@PathVariable String badgeId) {
        return badgeProcessingService.processBadgeScan(badgeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
