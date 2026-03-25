# IoT Access Control Simulation — Project Roadmap

> Full audit of the current codebase: what works, what is broken, what is missing, and what to build next.

---

## 1. What Is Good (Keep It)

| Area | What Works Well |
|---|---|
| Architecture | Clear microservice boundaries — core, cockpit, cache-loader, watchdog, IoT mock |
| Event pipeline | Kafka → SSE pipeline design is solid |
| Docker Compose | Infrastructure services (Postgres, Redis, Kafka, Mosquitto, NGINX) have health checks |
| Redis config | Proper `GenericJackson2JsonRedisSerializer` + TTL in both services |
| SSE service | `CopyOnWriteArrayList` handles concurrency correctly; timeout/completion callbacks remove dead emitters |
| Kafka consumer | `JsonDeserializer` with `setUseTypeHeaders(false)` and explicit trusted packages — correct approach |
| MQTT publisher | Spring Integration outbound channel is a clean abstraction |
| `AccessEventDTO` | Java record — lightweight and immutable |
| Watchdog concept | Basic HA failover idea (needs hardening, see below) |
| Multi-compose split | `docker-compose.vm1-app.yml`, `vm2-messaging.yml`, `vm3-data.yml` shows infra thinking |

---

## 2. Critical Bugs (Fix First)

These bugs prevent the system from running end-to-end.

### 2.1 — Person model does not match the database schema

**File:** [Person.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/model/Person.java)
**File:** [init.sql](deploy/postgres/init.sql)

The DB schema (`registered_people` table) has:
```
badge_id VARCHAR(64), full_name TEXT, role TEXT, is_active BOOLEAN
```

The JPA entity has:
```java
int badgeId, String firstName, String lastName, String email, String phoneNumber, String address
```

- `badge_id` is a `VARCHAR` like `"B-0001"` but the entity uses `int`.
- `full_name` is a single column but the entity splits it into `firstName` / `lastName`.
- `email`, `phoneNumber`, `address` columns do not exist in the DB.
- `is_active` and `role` fields are missing from the entity.
- No `@Table(name = "registered_people")` annotation — Hibernate looks for a table called `person`.

**Result:** Every badge scan returns 500.

**Fix:** Align the entity with the DB, or align the DB with the entity. Recommended entity:
```java
@Entity
@Table(name = "registered_people")
public class Person {
    @Id @GeneratedValue private UUID id;
    @Column(name = "badge_id") private String badgeId;
    @Column(name = "full_name")  private String fullName;
    private String role;
    @Column(name = "is_active")  private boolean active;
}
```

---

### 2.2 — `PersonRepository` declares UUID primary key but entity uses `int`

**File:** [PersonRepository.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/repository/PersonRepository.java)

```java
public interface PersonRepository extends JpaRepository<Person, UUID> {
    Optional<Person> findByBadgeId(int badgeId); // int but DB value is "B-0001"
}
```

- PK type in `JpaRepository<Person, UUID>` contradicts the `@Id int badgeId` on the entity.
- `findByBadgeId(int)` will never match `"B-0001"`.

**Fix:** Change signature to `findByBadgeId(String badgeId)` and fix the PK generics after fixing 2.1.

---

### 2.3 — Badge scan has no REST controller

The README documents `GET /api/people/{badgeId}` as the badge scan endpoint.
There is **no controller** that handles this route in `core-operational-backend`.
`BadgeProcessingService` exists but is never called from HTTP — only from `BadgeKafkaListener`.

**Fix:** Add a `PersonController`:
```java
@RestController
@RequestMapping("/api/people")
public class PersonController {
    @GetMapping("/{badgeId}")
    public ResponseEntity<?> scan(@PathVariable String badgeId) { ... }
}
```

---

### 2.4 — `EventProducer` is never called on badge scan

**File:** [BadgeProcessingService.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/service/BadgeProcessingService.java)

`BadgeProcessingService` calls MQTT but never calls `EventProducer`. Kafka audit log is never published on badge scan. The cockpit UI will never receive badge events via SSE.

**Fix:** Inject `EventProducer` into `BadgeProcessingService` and call `publishBadgeEvent()` after the MQTT publish.

---

### 2.5 — Kafka topic mismatch between producer and consumer

**File:** [EventProducer.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/service/EventProducer.java) — publishes to `entrance_attempts`

**File:** [AccessEventListener.java](app/services/entrance-cockpit-backend/src/main/java/upec/badge/entrance_cockpit_backend/service/AccessEventListener.java) — listens to `${kafka.topic.access-events}` (env var `KAFKA_DOOR_OPENING_TOPIC`)

These two topics are almost certainly different values. The cockpit-backend will never receive badge events.

**Fix:** Align both to a single topic name. Either hardcode `access-events` in both or pass it via an env var to both services.

---

### 2.6 — `AccessEventDTO` fields do not match what `EventProducer` publishes

**File:** [AccessEventDTO.java](app/services/entrance-cockpit-backend/src/main/java/upec/badge/entrance_cockpit_backend/dto/AccessEventDTO.java)

```java
record AccessEventDTO(String badgeId, String firstName, String lastName, String eventType, String timestamp)
```

**File:** [EventProducer.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/service/EventProducer.java)

```java
"{\"badgeId\":\"%s\",\"status\":\"%s\",\"timestamp\":\"%s\"}"
```

`EventProducer` sends `status` but `AccessEventDTO` expects `firstName`, `lastName`, `eventType`. Deserialization will silently produce nulls or throw an exception.

**Fix:** Make the Kafka JSON payload and the DTO match. Use `ObjectMapper` instead of `String.format()` to build the JSON (see 3.1).

---

### 2.7 — Missing environment variables in docker-compose

**File:** [docker-compose.all.yml](deploy/compose/docker-compose.all.yml)

`core-operational-backend` requires these env vars (from `application.properties`) but they are absent from compose:

| Variable | Required by |
|---|---|
| `POSTGRES_HOST` | datasource URL |
| `POSTGRES_PORT` | datasource URL |
| `REDIS_HOST` | Redis config |
| `REDIS_PORT` | Redis config |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka producer |
| `MQTT_HOST` | MqttConfig |
| `MQTT_PORT` | MqttConfig |

`entrance-cockpit-backend` requires:

| Variable | Required by |
|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | consumer config (property uses this name; compose sets `SPRING_KAFKA_BOOTSTRAP_SERVERS`) |
| `KAFKA_DOOR_OPENING_TOPIC` | topic name |
| `MQTT_HOST` | MQTT config |
| `MQTT_PORT` | MQTT config |
| `CORE_BACKEND_URL` | ManualControlService |

`cache-loader-backend` requires:
`POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`

**Fix:** Add all missing env vars to compose (or define them in the `.env` file).

---

### 2.8 — `KafkaProducerConfig` hardcodes `kafka:9092`

**File:** [KafkaProducerConfig.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/config/KafkaProducerConfig.java)

```java
config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
```

`application.properties` already defines `spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS}`. This manual config class overrides it with a hardcoded value and makes the env var useless.

**Fix:** Delete `KafkaProducerConfig.java` — Spring Boot's auto-configuration already reads `spring.kafka.*` properties correctly.

---

### 2.9 — Health check URL wrong for `core-operational-backend`

**File:** [docker-compose.all.yml](deploy/compose/docker-compose.all.yml) line 118

```yaml
test: ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
```

The service runs on port `8081`, not `8080`. Also, `spring-boot-actuator` is not in the `pom.xml`, so `/actuator/health` does not exist.

**Fix:** Either add `spring-boot-starter-actuator` to the pom, or use the `/status` endpoint that already exists:
```yaml
test: ["CMD-SHELL", "curl -f http://localhost:8081/status || exit 1"]
```

---

## 3. Code Quality Issues (Fix Soon)

### 3.1 — Manual JSON construction with `String.format()`

**Files:** `EventProducer.java`, `MqttDecisionPublisher.java`

Building JSON strings with `String.format()` is fragile — any value containing `"` or `\n` will produce invalid JSON.

**Fix:** Inject `ObjectMapper` and serialize a DTO or a `Map`.

---

### 3.2 — `PersonService` uses field injection

**File:** [PersonService.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/service/PersonService.java)

```java
@Autowired
private PersonRepository repository;
```

Field injection makes the class harder to test and hides its dependencies.

**Fix:** Replace with constructor injection.

---

### 3.3 — `ManualControlController` receives unused `EventProducer`

**File:** [ManualControlController.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/controller/ManualControlController.java)

`EventProducer kafkaProducer` is received in the constructor but never stored or used. Manual override events are not published to Kafka.

**Fix:** Either use it to publish a manual override event to Kafka, or remove the parameter.

---

### 3.4 — `ManualController` in cockpit-backend is missing `/close` endpoint

**File:** [ManualController.java](app/services/entrance-cockpit-backend/src/main/java/upec/badge/entrance_cockpit_backend/controller/ManualController.java)

`ManualControlService` has `sendCloseCommand()` implemented, but no HTTP endpoint exposes it.

**Fix:** Add `POST /api/manual/close` that calls `manualControlService.sendCloseCommand()`.

---

### 3.5 — `pom.xml` description copy-paste error

**File:** [pom.xml](app/services/core-operational-backend/pom.xml) line 16

```xml
<description>entrance-cockpit-backend service</description>
```

The core-operational-backend pom describes itself as entrance-cockpit-backend.

**Fix:** Change to `core-operational-backend service`.

---

### 3.6 — `App.css` is Vite boilerplate

**File:** [App.css](app/web/entrance-cockpit-front/src/App.css)

Contains default Vite/React logo spin styles that have nothing to do with the cockpit UI.

**Fix:** Clear the file or replace with project-relevant styles.

---

### 3.7 — `BadgeKafkaListener` creates a confusing circular dependency

**File:** [BadgeKafkaListener.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/service/BadgeKafkaListener.java)

`core-operational-backend` both **publishes to** and **listens from** `entrance_attempts`. Nothing external currently publishes to this topic, so the listener is dead code.

The correct flow is: HTTP request → `PersonController` → `BadgeProcessingService` → Kafka publish. There is no reason for core-backend to consume from Kafka in the current design.

**Fix:** Remove `BadgeKafkaListener` unless there is a specific design intent to decouple via Kafka (which should be documented).

---

## 4. Missing Implementations

### 4.1 — `cache-loader-backend` has no loader logic

The service has a `main` class and a `RedisConfig` but no actual loader.
There is no repository, no entity, no service that reads from PostgreSQL and writes to Redis.

**Fix:** Implement:
- `PersonEntity` (mirroring the DB schema)
- `PersonJdbcRepository` or JPA repository
- `CacheLoaderService` with a `@PostConstruct` or `CommandLineRunner` that reads all active people and writes them to Redis with `person:{badgeId}` keys

---

### 4.2 — `core-operational-backend` has no Redis lookup for badge scan

The README says: "Redis-first badge validation with a PostgreSQL fallback."

`PersonService` only calls the JPA repository (PostgreSQL). There is no Redis read.

**Fix:** In `PersonService.getPersonById()`, first check Redis (`redisTemplate.opsForValue().get("person:" + badgeId)`), and only fall back to PostgreSQL if not found.

---

### 4.3 — No CORS configuration

The frontend calls the backend. In development (different ports) this will be blocked by the browser.

**Fix:** Add a `@CrossOrigin` annotation or a `WebMvcConfigurer` bean that allows requests from the frontend origin.

---

### 4.4 — No global exception handler

Any unhandled exception returns a Spring Boot default error response. Clients get HTML error pages instead of structured JSON.

**Fix:** Add a `@RestControllerAdvice` class with `@ExceptionHandler` for at least `NotFoundException` and generic `Exception`.

---

### 4.5 — No `.env` file committed (or documented)

The compose file uses many variables (`${POSTGRES_USER}`, `${POSTGRES_DB}`, etc.) that should come from a `.env` file. There is no `.env.example` to guide developers.

**Fix:** Add a `deploy/compose/.env.example` with all required variables and safe defaults.

---

### 4.6 — Watchdog has hardcoded container names and no reconnect logic

**File:** [WatchdogService.java](app/services/watchdog-app/src/main/java/com/upec/watchdogapp/WatchdogService.java)

- `TARGET_URL` and `BACKUP_CONTAINER` are hardcoded strings, not config values.
- Once backup starts, monitoring stops forever (no check if B is healthy, no attempt to restore A).
- Spawning `docker start` from within a container requires Docker socket mounting — a significant security concern.
- Indentation is inconsistent (line 14 is not indented).

**Fix (short term):** Move URLs/names to `application.properties`. Add a timeout/retry before declaring instance A dead. Document the Docker socket requirement.

**Fix (long term):** Replace with a proper HA mechanism (Kubernetes liveness probes, Docker Swarm, or simply a load balancer in front of two instances).

---

## 5. Phase Roadmap

### Phase 1 — Bug Fixes (Highest Priority)

Get the end-to-end flow working for the first time.

- [ ] Fix `Person` entity to match `registered_people` table schema (bug 2.1)
- [ ] Fix `PersonRepository` PK type and `findByBadgeId` signature (bug 2.2)
- [ ] Add `PersonController` with `GET /api/people/{badgeId}` (bug 2.3)
- [ ] Call `EventProducer` inside `BadgeProcessingService` (bug 2.4)
- [ ] Align Kafka topic names between producer and consumer (bug 2.5)
- [ ] Align `AccessEventDTO` fields with what `EventProducer` serializes (bug 2.6)
- [ ] Add all missing env vars to `docker-compose.all.yml` (bug 2.7)
- [ ] Delete `KafkaProducerConfig.java` — let Spring Boot auto-configure (bug 2.8)
- [ ] Fix health check port and endpoint for core backend (bug 2.9)

**Acceptance test:** `curl http://localhost:8080/api/people/B-0001` returns a person JSON, the door-lock-mock logs `OPEN`, and the cockpit UI live stream shows the event.

---

### Phase 2 — Missing Core Features

Fill in the gaps in documented behavior.

- [ ] Implement `cache-loader-backend` loader logic (4.1)
- [ ] Implement Redis-first lookup in `PersonService` (4.2)
- [ ] Add `POST /api/manual/close` to cockpit-backend (3.4)
- [ ] Publish manual override event to Kafka in `ManualControlController` (3.3)
- [ ] Add CORS configuration (4.3)
- [ ] Add global exception handler (4.4)
- [ ] Add `deploy/compose/.env.example` (4.5)

**Acceptance test:** Cache loader populates Redis on startup; badge scan hits Redis first; manual close sends MQTT command; all errors return structured JSON.

---

### Phase 3 — Code Quality

Clean up the code without changing behavior.

- [ ] Replace `String.format()` JSON with `ObjectMapper` serialization (3.1)
- [ ] Replace field injection with constructor injection in `PersonService` (3.2)
- [ ] Remove `BadgeKafkaListener` or document its purpose (3.7)
- [ ] Fix pom.xml description in core-operational-backend (3.5)
- [ ] Remove Vite boilerplate from `App.css` (3.6)
- [ ] Review watchdog hardcoded values and Docker socket usage (4.6)

---

### Phase 4 — Hardening & Observability (from original roadmap)

Make the system production-ready.

- [ ] Add `spring-boot-starter-actuator` to all Spring Boot services
- [ ] Add Prometheus metrics endpoint (`/actuator/prometheus`)
- [ ] Add Grafana dashboard for Kafka consumer lag, HTTP latency, Redis hit/miss rate
- [ ] Add Loki for log aggregation
- [ ] Implement Docker secrets or a vault for credentials (replace plain env vars)
- [ ] Add integration tests using Testcontainers for each service
- [ ] Set up GitHub Actions CI pipeline: build → test → Docker image push

---

### Phase 5 — Future Enhancements (from original roadmap)

- [ ] Multi-entrance support — topic per door, separate MQTT topics
- [ ] Role-based access — badge type determines which doors it can open
- [ ] Visitor badges — temporary badge IDs with expiry stored in Redis TTL
- [ ] Badge scan simulator — Node.js or Python script that sends automated scan traffic
- [ ] Advanced analytics — peak hours, denial patterns, per-person history
- [ ] `docker-compose.production.yml` — production-tuned compose with resource limits

---

## 6. Quick Reference: File → Issue Map

| File | Issue |
|---|---|
| [Person.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/model/Person.java) | Schema mismatch with DB (2.1) |
| [PersonRepository.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/repository/PersonRepository.java) | Wrong PK type and field type (2.2) |
| [PersonService.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/service/PersonService.java) | No Redis lookup; field injection (2.2, 3.2, 4.2) |
| [BadgeProcessingService.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/service/BadgeProcessingService.java) | Never calls EventProducer (2.4) |
| [EventProducer.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/service/EventProducer.java) | Wrong topic name; manual JSON (2.5, 3.1) |
| [MqttDecisionPublisher.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/service/MqttDecisionPublisher.java) | Manual JSON construction (3.1) |
| [BadgeKafkaListener.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/service/BadgeKafkaListener.java) | Dead code / circular flow (3.7) |
| [ManualControlController.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/controller/ManualControlController.java) | Unused EventProducer; no Kafka publish (3.3) |
| [KafkaProducerConfig.java](app/services/core-operational-backend/src/main/java/upec/badge/core_operational_backend/config/KafkaProducerConfig.java) | Hardcoded broker address — delete this file (2.8) |
| [AccessEventDTO.java](app/services/entrance-cockpit-backend/src/main/java/upec/badge/entrance_cockpit_backend/dto/AccessEventDTO.java) | Fields don't match Kafka payload (2.6) |
| [ManualController.java](app/services/entrance-cockpit-backend/src/main/java/upec/badge/entrance_cockpit_backend/controller/ManualController.java) | Missing /close endpoint (3.4) |
| [CacheLoaderBackendApplication.java](app/services/cache-loader-backend/src/main/java/upec/badge/cache_loader_backend/CacheLoaderBackendApplication.java) | No loader service implemented (4.1) |
| [WatchdogService.java](app/services/watchdog-app/src/main/java/com/upec/watchdogapp/WatchdogService.java) | Hardcoded values; Docker socket security (4.6) |
| [docker-compose.all.yml](deploy/compose/docker-compose.all.yml) | Missing env vars; wrong health check (2.7, 2.9) |
| [pom.xml (core)](app/services/core-operational-backend/pom.xml) | Wrong description (3.5) |
| [App.css](app/web/entrance-cockpit-front/src/App.css) | Vite boilerplate (3.6) |
