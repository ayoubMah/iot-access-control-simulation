package upec.badge.cache_loader_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "registered_people")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "badge_id", unique = true, nullable = false)
    private String badgeId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String role;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public Person() {
    }

    public UUID getId() { return id; }
    public String getBadgeId() { return badgeId; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
}
