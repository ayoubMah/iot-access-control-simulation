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

- [x] `cache-loader-backend` fully implemented: `Person` entity, `PersonRepository`, `CacheLoadingRunner` (`CommandLineRunner`) that reads all active people from PostgreSQL and writes `person:{badgeId}` keys to Redis
- [x] Redis-first lookup implemented in `PersonService` — PostgreSQL fallback with write-back on miss
- [x] Added `POST /api/manual/close` to cockpit-backend
- [x] Manual override events (open/close) now published to Kafka via `EventProducer.publishManualEvent()`
- [x] CORS configuration added to both `core-operational-backend` and `entrance-cockpit-backend`
- [x] Global exception handler (`@RestControllerAdvice`) added to `core-operational-backend` — structured JSON error responses
- [x] `deploy/compose/.env.example` created with all required variables and safe defaults

### Phase 3 — Code Quality

- [x] Replaced `String.format()` JSON construction with `ObjectMapper` in `EventProducer` and `MqttDecisionPublisher`
- [x] Replaced field injection (`@Autowired`) with constructor injection in `PersonService`
- [x] Removed `BadgeKafkaListener` — dead code causing a confusing circular flow
- [x] Fixed copy-paste `pom.xml` descriptions in `core-operational-backend` and `cache-loader-backend`
- [x] Cleared Vite boilerplate from `App.css`
- [x] Watchdog config externalised to `application.properties` (`watchdog.target-url`, `watchdog.backup-container`, `watchdog.check-interval-ms`)
- [x] Replaced `System.out.println` / `e.printStackTrace()` with SLF4J logger in `WatchdogService`
- [x] Deploy folder consolidated: removed `docker-compose.vm1-app.yml`, `vm2-messaging.yml`, `vm3-data.yml`; `docker-compose.all.yml` is the single source of truth
- [x] Kafka Compose healthcheck added; all service dependencies upgraded to `service_healthy` where applicable
- [x] NGINX volume mount path corrected to `conf.d/default.conf`

---

## Phase 4 — Hardening & Observability (Next)

Make the system production-ready.

- [ ] Add `spring-boot-starter-actuator` to all Spring Boot services
- [ ] Expose Prometheus metrics endpoint (`/actuator/prometheus`) on each service
- [ ] Add Grafana dashboard: Kafka consumer lag, Redis hit/miss rate, HTTP latency per service
- [ ] Add Loki for log aggregation (structured JSON logs from all containers)
- [ ] Replace plain-text `.env` credentials with Docker secrets
- [ ] Add integration tests with Testcontainers for `core-operational-backend` and `entrance-cockpit-backend`
- [ ] Set up GitHub Actions CI pipeline: build → test → push Docker image to registry
- [ ] Add `restart: unless-stopped` policy and resource limits to all compose services

---

## Phase 5 — Future Enhancements

- [ ] Multi-entrance support — one MQTT topic per door, per-door access policies
- [ ] Role-based access — badge type determines which doors it can open
- [ ] Visitor badges — temporary badge IDs with Redis TTL expiry
- [ ] Badge scan simulator — automated traffic generator (Node.js or Python)
- [ ] Advanced analytics — peak hours, denial patterns, per-person history
- [ ] Watchdog long-term replacement — Kubernetes liveness probes or Docker Swarm health management
