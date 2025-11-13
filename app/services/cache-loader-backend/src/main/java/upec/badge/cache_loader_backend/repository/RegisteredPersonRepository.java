package upec.badge.cache_loader_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upec.badge.cache_loader_backend.model.RegisteredPerson;

import java.util.UUID;
import java.util.Optional;

public interface RegisteredPersonRepository extends JpaRepository<RegisteredPerson, UUID> {
    Optional<RegisteredPerson> findByBadgeId(String badgeId);
}
