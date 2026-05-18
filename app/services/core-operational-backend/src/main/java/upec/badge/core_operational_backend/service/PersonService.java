package upec.badge.core_operational_backend.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import upec.badge.shared.model.Person;
import upec.badge.core_operational_backend.repository.PersonRepository;

@Service
public class PersonService {

    private static final Logger log = LoggerFactory.getLogger(PersonService.class);
    private static final String CACHE_KEY_PREFIX = "person:";

    private final PersonRepository repository;
    private final RedisTemplate<String, Person> redisTemplate;

    public PersonService(PersonRepository repository, RedisTemplate<String, Person> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    public Optional<Person> getPersonByBadgeId(String badgeId) {
        String key = CACHE_KEY_PREFIX + badgeId;

        Person cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.debug("Cache HIT for badgeId={}", badgeId);
            return Optional.of(cached);
        }

        log.debug("Cache MISS for badgeId={} — falling back to PostgreSQL", badgeId);
        Optional<Person> result = repository.findByBadgeId(badgeId);
        result.ifPresent(p -> redisTemplate.opsForValue().set(key, p));
        return result;
    }
}
