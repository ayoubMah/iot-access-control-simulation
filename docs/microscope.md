# 🧠 Microscope — Step-by-Step Execution Plan (with Delegation)

A living, incremental roadmap for the Badge Entrance Simulation project.
Each phase lists ownership, deliverables, and "done" criteria.
Use GitHub issues or Markdown checkboxes to track progress.

---

## 👥 Roles Overview

| Person | Focus | Keywords |
|--------|-------|----------|
| **AyoubMah** | Infra orchestration, DevOps, Docs | Docker · Compose · NGINX · Makefile · Integration |
| **AyoubOua** | Core business logic & backend services | Spring Boot · Kafka · MQTT · Redis · PostgreSQL |
| **Hamza** | Messaging & IoT simulation layer | Node.js · MQTT · Kafka · Bridge · Mocks |

---

## 🧱 Phase 0 — Bootstrap & Infrastructure

**Owner:** Ayoub

**Goal:** `docker compose up` starts all infra and DB has seeded users.

### Tasks

- [x] Add base Docker Compose with: PostgreSQL, Redis, Kafka + Zookeeper, Mosquitto, NGINX.
- [x] Seed DB with sample data via `init.sql`.
- [x] Configure healthcheck for all containers.
- [x] Verify connectivity:
  - `psql` → 4 users
  - `redis-cli ping` → PONG
  - `curl localhost:8080/healthz` → ok

### ✅ Done When

- All infra services reach healthy state.
- Logs show no connection retries.

---

## ⚙️ Phase 1 — Service Skeletons

**Owner:** Ouadra (backend) + Hamza (Node) + Ayoub (Docker)

### Tasks

- [ ] Scaffold Spring Boot apps:
  - `core-operational-backend`
  - `entrance-cockpit-backend`
  - `cache-loader-backend`
- [ ] Scaffold Node.js app:
  - `telemetry-messaging-backend`
- [ ] Add Dockerfiles for each; join shared network.

### ✅ Done When

`docker compose build` completes and all services start HEALTHY.

---

## 🔐 Phase 2 — Messaging & Contracts

**Owner:** Ayoub (docs) · Ouadra (Kafka) · Hamza (MQTT)

### Tasks

- [ ] Define topics:
  - **MQTT** → `iot/entrance/badge`, `iot/entrance/decision`
  - **Kafka** → `attempt-logs`, `entrance-logs`
- [ ] Document payload schemas in `/docs/messaging`.
- [ ] Provision topics via script or auto-create.

### ✅ Done When

Both Kafka and MQTT topics confirmed reachable.

---

## 🧪 Phase 3 — IoT Mocks

**Owner:** Hamza

### Tasks

- [ ] `badge-sensor-mock` → publishes badge scans (`badge_id`, `timestamp`)
- [ ] `door-lock-mock` → subscribes to `iot/entrance/decision`, logs OPEN/DENIED
- [ ] Add both to Compose (optional profiles).

### ✅ Done When

Publishing a badge scan triggers console output in lock mock.

---

## 🔄 Phase 4 — MQTT ↔ Kafka Bridge

**Owner:** Hamza

### Tasks

- [ ] Implement `telemetry-messaging-backend`:
  - MQTT sub → `iot/entrance/badge`
  - Kafka pub → `attempt-logs`
- [ ] Optionally mirror `iot/entrance/decision` → `entrance-logs`.

### ✅ Done When

`kafka-console-consumer` shows forwarded MQTT messages.

---

## 🧠 Phase 5 — Core Operational Logic

**Owner:** Ouadra

### Tasks

- [ ] Redis-first lookup + PostgreSQL fallback for badge validation.
- [ ] Decision policy: user exists & active → GRANTED.
- [ ] Publish decision → `iot/entrance/decision` (MQTT).
- [ ] Log all attempts to Kafka (`attempt-logs`, `entrance-logs`).

### ✅ Done When

Badge scan results in both decision MQTT message and Kafka event.

---

## 🗄️ Phase 6 — Cache Loader

**Owner:** Ouadra

### Tasks

- [ ] Scheduled job sync DB → Redis periodically.
- [ ] Evict disabled users.

### ✅ Done When

Disabling a user in DB reflects in Redis next cycle.

---

## 📡 Phase 7 — Cockpit Backend & UI

**Owner:** Ouadra (back) + Ayoub (front & proxy)

### Tasks

- [ ] **Backend:** Kafka consumer for `attempt-logs` & `entrance-logs`; WebSocket to UI; manual authorize endpoint → MQTT publish.
- [ ] **Front:** static HTML/JS dashboard; show live table; manual actions.

### ✅ Done When

UI updates < 2s after scan; manual authorize opens lock mock.

---

## 🌐 Phase 8 — NGINX Gateway

**Owner:** Ayoub

### Tasks

- [ ] Configure TLS (self-signed), routes for cockpit backend + static UI + WebSocket proxy.
- [ ] Add rate limits and access logs.

### ✅ Done When

All front/back traffic goes through NGINX.

---

## 📊 Phase 9 — Observability (optional)

**Owner:** Ayoub

### Tasks

- [ ] Add Prometheus + Grafana + Loki (via Compose).
- [ ] Expose metrics endpoints on each service.

### ✅ Done When

Dashboard shows Kafka lag, MQTT rate, JVM metrics.

---

## 🚀 Phase 10 — CI/CD & Testing

**Owner:** Ayoub (+ support from both)

### Tasks

- [ ] GitHub Actions workflow:
  - Build all images
  - Run unit + integration tests
  - Spin Compose for validation
- [ ] Java → Testcontainers; Node → Jest.

### ✅ Done When

Full CI pipeline green on every PR.

---

## 🛡️ Phase 11 — Security & Hardening

**Owner:** Ayoub (lead) · Ouadra (back policies)

### Tasks

- [ ] Secrets via `.env` / Docker secrets
- [ ] TLS inside Compose (optional mTLS)
- [ ] Simple RBAC for cockpit admin

### ✅ Done When

Secrets not committed; TLS enforced; admin path protected.

---

## 🧰 Quick Commands

```bash
# Start full stack
docker compose -f deploy/docker-compose.yml up -d

# Kafka: consume topic
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic attempt-logs --from-beginning

# MQTT: simulate badge
docker exec mosquitto mosquitto_pub \
  -h mosquitto -t iot/entrance/badge \
  -m '{"badge_id":"A12345","timestamp":"2025-01-01T00:00:00Z"}'
```

---

## 💡 Backlog / Future Ideas

- Multi-door / multi-site topic partitioning
- Avro + Schema Registry
- Graceful degradation on service failures
- Audit dashboard + CSV export