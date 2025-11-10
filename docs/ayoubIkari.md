# Ouadra Scope — Core Logic & Service Architecture

> **Scope:** Mid‑level complexity — Spring Boot microservices, badge validation logic, Redis/PostgreSQL interaction, Kafka & MQTT integration, and cockpit backend with WebSocket streaming.

---

##  Objectives

* Implement **business logic** around badge authorization.
* Publish decisions to **MQTT topics** for IoT mocks.
* Keep **Redis cache synchronized** with DB using a cache‑loader service.
* Expose **Cockpit Backend API** with WebSocket updates.

---

##  Ownership & Interfaces

* **Owner:** Ouadra
* **Interfaces:**

  * From AyoubMah → uses Kafka/MQTT/DB infrastructure.
  * From Hamza → consumes badge events via Kafka (`attempt-logs`).
* **You provide:** Spring services, application logic, and unit/integration tests.

---

##  Services to Build

| Service                      | Role                                            |
| :--------------------------- | :---------------------------------------------- |
| **core-operational-backend** | Authorize badges, publish decisions, log events |
| **cache-loader-backend**     | Sync Redis with PostgreSQL                      |
| **entrance-cockpit-backend** | Serve cockpit UI data via REST & WebSocket      |

All in Java 21 + Spring Boot 3.x (Gradle or Maven).

---

##  Structure Example

```
/core-operational-backend
  ├── src/main/java/upec/badge/core/
  │     ├── controller/
  │     ├── service/
  │     ├── config/
  │     └── model/
  ├── resources/application.yml
/cache-loader-backend
  ├── ...
/entrance-cockpit-backend
  ├── ...
```

---

##  Step 1 — Core Operational Backend

### Purpose

Consumes badge scans, checks Redis/PostgreSQL, decides `GRANTED` or `DENIED`, publishes both MQTT and Kafka events.

### Tasks

* [ ] Add dependencies: `spring-boot-starter-web`, `spring-kafka`, `spring-data-redis`, `spring-data-jpa`, `spring-integration-mqtt`.
* [ ] Configuration (`application.yml`):

  ```yaml
  spring:
    datasource:
      url: jdbc:postgresql://postgres:5432/badge_db
      username: postgres
      password: postgres
    data:
      redis:
        host: redis
        port: 6379
    kafka:
      bootstrap-servers: kafka:9092
  ```
* [ ] `PeopleController` → endpoint `/api/people/{badgeId}` to test logic manually.
* [ ] `PeopleService` → lookup badgeId in Redis; fallback to Postgres if not cached.
* [ ] `EventProducer` (Kafka) → publish structured JSON event.
* [ ] `MqttDecisionPublisher` → publish decision to `iot/entrance/decision`.

**Done when:**

* `GET /api/people/{badgeId}` returns `GRANTED` or `DENIED`.
* Kafka topic `entrance-logs` receives an event.
* MQTT `iot/entrance/decision` topic emits a decision message.

---

##  Step 2 — Kafka Consumer Integration

### Purpose

Consume events from `attempt-logs` (produced by Hamza’s bridge).

### Tasks

* [ ] Create `@KafkaListener` for `attempt-logs`.
* [ ] Deserialize message `{badge_id, timestamp}`.
* [ ] Call validation logic and trigger decision publication.

**Done when:**
Badge scans sent by mock → trigger automatic validation and MQTT decision.

---

##  Step 3 — Cache Loader Backend

### Purpose

Sync DB → Redis periodically.

### Tasks

* [ ] New Spring Boot app with `@EnableScheduling`.
* [ ] Use `@Scheduled(fixedRate = 60000)` for sync job.
* [ ] Query DB: `SELECT badge_id, name, is_active FROM registered_people`.
* [ ] Store in Redis as `user:{badge_id}` → JSON string.
* [ ] Remove entries for disabled users.

**Done when:**
Redis always reflects DB state within sync interval.

---

##  Step 4 — Cockpit Backend

### Purpose

Provide WebSocket stream of events + REST API for manual decisions.

### Tasks

* [ ] REST endpoints:

  * `GET /api/logs` → recent Kafka messages.
  * `POST /api/manual-decision` → payload `{badge_id, decision}` → publish MQTT decision.
* [ ] WebSocket `/ws/logs` → push Kafka updates in real time.
* [ ] Use `spring-websocket` and `@SendTo` or `SimpMessagingTemplate`.

**Done when:**
UI updates live when Kafka emits new messages and manual action works.

---

##  Step 5 — Testing

* [ ] Use **Testcontainers** for integration tests (PostgreSQL, Redis, Kafka, Mosquitto optional).
* [ ] Unit test: `PeopleService`, `EventProducer`, `MqttDecisionPublisher`.
* [ ] Mock KafkaTemplate + MessageChannel in tests.

**Done when:**
`mvn test` passes and logs confirm valid Kafka+Redis integration.

---

##  Commands & Verification

```bash
# Hit manual validation endpoint
docker exec core-operational curl http://localhost:8081/api/people/B-001

# Kafka consumer check
docker exec kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic entrance-logs --from-beginning

# MQTT decision check
docker exec mosquitto mosquitto_sub -t iot/entrance/decision
```

---

##  Interfaces

* **Consumes:** Kafka `attempt-logs` (from Hamza).
* **Produces:** Kafka `entrance-logs` (authorized attempts).
* **Publishes:** MQTT `iot/entrance/decision` (decision for door lock mock).
* **Reads/Writes:** PostgreSQL, Redis.

---

##  Acceptance Criteria

* A badge scan from Hamza’s mock triggers validation and MQTT decision in <2s.
* Cache loader keeps Redis consistent with DB.
* Cockpit backend streams Kafka logs to the front-end via WebSocket.
* Unit + integration tests are green.
