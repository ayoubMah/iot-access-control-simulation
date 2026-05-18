package upec.badge.cache_loader_backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import upec.badge.shared.model.Person;

public interface PersonRepository extends JpaRepository<Person, UUID> {
}
