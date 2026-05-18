# IoT Access Control Simulation — Project Roadmap

> This document tracks what was fixed, what was built, and what comes next.

---

## Phases 1–3 — Completed

All three refactoring phases have been merged to `master`. The system now runs end-to-end.

### Phase 1 — Critical Bug Fixes

These bugs prevented the system from running at all.

- [x] Aligned `Person` entity with `registered_people` table schema (`badge_id VARCHAR`, `full_name`, `role`, `is_active`; added `@Table` annotation)
- [x] Fixed `PersonRepository` — PK type `UUID`, `findByBadgeId(String)` signature
- [x] Added `PersonController` — `GET /api/people/{badgeId}` (was completely missing)
- [x] `BadgeProcessingService` now calls `EventProducer` on every badge scan
- [x] Unified Kafka topic name: `access-events` via `KAFKA_ACCESS_EVENTS_TOPIC` env var in both producer and consumer
- [x] `AccessEventDTO` fields aligned with producer payload: `(badgeId, fullName, status, eventType, timestamp)`
- [x] All missing env vars added to `docker-compose.all.yml` (`POSTGRES_HOST/PORT`, `REDIS_HOST/PORT`, `KAFKA_BOOTSTRAP_SERVERS`, `MQTT_HOST/PORT`, `CORE_BACKEND_URL`, `INSTANCE_ID`)
- [x] Deleted `KafkaProducerConfig.java` — was hardcoding `kafka:9092`, overriding Spring Boot auto-config
- [x] Fixed core backend healthcheck: port `8081`, endpoint `/status`, using `wget` (switched runtime to `eclipse-temurin:21-jre-alpine`)

### Phase 2 — Missing Core Features

- [x] `cache-loader-backend` fully implemented (entity, repository, `CommandLineRunner`)
- [x] Redis-first lookup implemented in `PersonService` — PostgreSQL fallback with write-back on miss
- [x] Added `POST /api/manual/close` to cockpit-backend
- [x] Manual override events (open/close) now published to Kafka
- [x] CORS configuration added to both `core-operational-backend` and `entrance-cockpit-backend`
- [x] Global exception handler (`@RestControllerAdvice`) added to `core-operational-backend`
- [x] `deploy/compose/.env.example` created with all required variables and safe defaults

### Phase 3 — Code Quality

- [x] Replaced `String.format()` JSON construction with `ObjectMapper` in `EventProducer` and `MqttDecisionPublisher`
- [x] Replaced field injection (`@Autowired`) with constructor injection in `PersonService`
- [x] Removed `BadgeKafkaListener` — dead code causing a confusing circular flow
- [x] Fixed copy-paste `pom.xml` descriptions
- [x] Deploy folder consolidated: removed `docker-compose.vm1-app.yml`, `vm2-messaging.yml`, `vm3-data.yml`
- [x] Kafka Compose healthcheck added; all service dependencies upgraded to `service_healthy` where applicable
- [x] NGINX volume mount path corrected to `conf.d/default.conf`

---

## Phase 4 — Reliability Fixes (Done)

Bugs the system had after Phases 1–3, plus repo hygiene.

- [x] **MQTT contract fixed.** `MqttDecisionPublisher` now emits `status` (`GRANTED` / `OPEN` / `CLOSE`) and the door-lock mock reads `badgeId` (camelCase) — previously the door never opened on grant because the mock looked for `badge_id` and a `status` field the publisher never sent.
- [x] **Shared `Person` model extracted.** New `shared-model` Maven module consumed by both `core-operational-backend` and `cache-loader-backend`. Redis values were previously written by one service and unreadable by the other because `GenericJackson2JsonRedisSerializer` embedded the FQN. Both `RedisConfig`s now use `Jackson2JsonRedisSerializer<Person>` — plain JSON, no `@class`.
- [x] **Manual override now reaches the door.** `ManualControlController` publishes an MQTT event on both open and close, not only open.
- [x] **Stranded services removed.** `watchdog-app`, `telemetry-messaging-backend`, `iot/badging-mock` were in the tree but not in compose. Watchdog was documented as HA but pointed at a service name that didn't exist.
- [x] **`spring-boot-devtools` removed** from runtime scope in `core`, `cache-loader`, `cockpit` poms.
- [x] **`spring.jpa.show-sql`** now defaults to `false` (overridable via `JPA_SHOW_SQL` env var).
- [x] **`restart: unless-stopped`** added to all long-running services in compose.
- [x] **Repo hygiene.** `.env` files untracked (only `.env.example` is committed). Top-level `.gitignore` broadened to cover `target/`, `node_modules/`, `dist/`, `*.log`.
- [x] **Cockpit cleanup.** Dead MQTT dependency and dead imports removed from `entrance-cockpit-backend`.
- [x] **README rewritten** to match the actual stack — no smart quotes, no macOS-only `open` command, no references to deleted services.

---

## Phase 5 — Hardening & Observability (Next)

Make the system production-ready.

- [ ] Add `spring-boot-starter-actuator` to all Spring Boot services
- [ ] Expose Prometheus metrics endpoint (`/actuator/prometheus`) on each service
- [ ] Add Grafana dashboard: Kafka consumer lag, Redis hit/miss rate, HTTP latency per service
- [ ] Add Loki for log aggregation (structured JSON logs from all containers)
- [ ] Replace plain-text `.env` credentials with Docker secrets
- [ ] Add integration tests with Testcontainers for `core-operational-backend` and `entrance-cockpit-backend`
- [ ] Set up GitHub Actions CI pipeline: build → test → push Docker image to registry
- [ ] Add resource limits (CPU/memory) to all compose services

---

## Phase 6 — Future Enhancements

- [ ] Multi-entrance support — one MQTT topic per door, per-door access policies
- [ ] Role-based access — badge type determines which doors it can open
- [ ] Visitor badges — temporary badge IDs with Redis TTL expiry
- [ ] Badge scan simulator — automated traffic generator
- [ ] Advanced analytics — peak hours, denial patterns, per-person history
- [ ] HA replacement — Kubernetes liveness probes or Docker Swarm health management
