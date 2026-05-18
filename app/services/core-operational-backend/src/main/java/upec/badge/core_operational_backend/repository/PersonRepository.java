package upec.badge.core_operational_backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import upec.badge.shared.model.Person;

public interface PersonRepository extends JpaRepository<Person, UUID> {
    Optional<Person> findByBadgeId(String badgeId);
}