package upec.badge.cache_loader_backend.runner;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import upec.badge.cache_loader_backend.model.Person;
import upec.badge.cache_loader_backend.repository.PersonRepository;

@Component
public class CacheLoadingRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CacheLoadingRunner.class);

    private final PersonRepository personRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ConfigurableApplicationContext context;

    public CacheLoadingRunner(
            PersonRepository personRepository,
            RedisTemplate<String, Object> redisTemplate,
            ConfigurableApplicationContext context
    ) {
        this.personRepository = personRepository;
        this.redisTemplate = redisTemplate;
        this.context = context;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("============== CACHE LOADER STARTED ==============");

        try {
            // 1. Fetch all people from PostgreSQL
            List<Person> people = personRepository.findAll();
            if (people.isEmpty()) {
                logger.warn("No registered people found in the database. Cache will be empty.");
            } else {
                logger.info("Found {} records in the database. Loading into Redis cache...", people.size());
            }

            // 2. Load each person into the Redis cache
            for (Person person : people) {
                // The cache key "person:{badgeId}" must match the format used by
                // PersonService in core-operational-backend to ensure cache hits.
                String cacheKey = "person:" + person.getBadgeId();
                redisTemplate.opsForValue().set(cacheKey, person);
                logger.debug("Cached: {} -> {}", cacheKey, person.getFullName());
            }

            logger.info("Successfully loaded {} records into Redis.", people.size());

        } catch (Exception e) {
            logger.error("!!! CRITICAL ERROR during cache loading process !!!", e);
        } finally {
            logger.info("============== CACHE LOADER FINISHED ==============");
            // 3. Shutdown the application with a success exit code.
            // This makes it a one-shot task, perfect for batch jobs.
            context.close();
        }
    }
}