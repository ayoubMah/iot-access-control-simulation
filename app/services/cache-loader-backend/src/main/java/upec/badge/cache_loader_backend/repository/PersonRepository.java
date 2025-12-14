package upec.badge.cache_loader_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import upec.badge.cache_loader_backend.model.Person;

public interface PersonRepository extends JpaRepository<Person, String> {
    Optional<Person> findByBadgeId(String badgeId);
}
