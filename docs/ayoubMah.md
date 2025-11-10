# AyoubMah Telescope — Infra Playbook

> **Scope:** project bootstrap, infra, security, CI/CD, observability, and integration quality.

---

## Objectives

* Establish **reliable local platform** (Docker Compose) with secure defaults.
* Produce and consume **Kafka events** cleanly using Spring Kafka.
* Provide **stable contracts** so Ouadra (core logic) & Hamza (messaging/mocks) can work **in isolation**.
* Enforce **delivery quality** via CI/CD, tests, and code reviews.

---

## 👤 Ownership & Interfaces

* **Owner:** ayoubMah (Infra / Gatekeeper)
* **Interfaces:**

  * **Ouadra:** consumes Kafka/MQTT; connects Redis/PostgreSQL; provides REST/WebSocket endpoints.
  * **Hamza:** implements MQTT sensor/lock mocks, MQTT↔Kafka bridge.
* **Me:** Compose environment, NGINX gateway, topic & schema contracts, CI/CD, observability hooks, shared Makefile.

---

## Pre‑requisites

* Docker Desktop / Engine ≥ 24.x, Compose v2
* mkcert or openssl (for local TLS)
* Java 21, Node.js LTS (for local builds)
* GNU Make

---

##  BP

1. **Platform Up:** Compose with healthchecks, secrets, TLS.
2. **Contracts Ready:** Topics, payload JSON Schemas, REST/OpenAPI stubs.
3. **Observability Online:** Prometheus+Grafana, Loki (optional).
4. **CI/CD:** Build, test, integration checks on PR.
5. **Quality Gate:** Code review rules & pre‑commit hooks.

---

##  Repo Layout (authoritative)

```
/app
  /services
    /core-operational-backend
    /entrance-cockpit-backend
    /cache-loader-backend
    /telemetry-messaging-backend
    /iot
      /badge-sensor-mock
      /door-lock-mock
/web/entrance-cockpit-front
/deploy
  docker-compose.yml
  .env.example
  nginx/
    nginx.conf
    certs/ (dev certs)
  kafka/
    topics-init.sh
    topics.env
  postgres/
    init.sql
  mosquitto/
    config/mosquitto.conf
  observability/
    prometheus.yml
    grafana/provisioning/**
    loki/config.yml (optional)
/docs
  messaging/
    mqtt.json
    attempt-logs.schema.json
    entrance-logs.schema.json
  api/
    openapi.yaml
  AyoubTelescope.md (this file)
  Microscope.md
Makefile
```

---

## ✅ Step 0 — Bootstrap the Repo

* [x] Create base folders shown above.
* [x] Add root `.editorconfig` and `.gitignore` (include `/deploy/**/certs/*` ignored except template).
* [x] Commit a **`CODEOWNERS`** file (Ayoub reviews `/deploy`, `/docs`, workflows).
* [x] Add PR template with checklist (tests, schema bump, docs updated).

**Done when:** `git clone` + `make up` brings stack healthy.

---

##  Step 1 — Compose Platform (hardening‑first)

* [x] Author `deploy/docker-compose.yml` with **explicit healthchecks** for: Postgres, Redis, Kafka, Zookeeper, Mosquitto, NGINX, Prometheus, Grafana.
* [x] Network segregation: `frontend`, `backend`, `observability` networks.
* [ ] Volumes for data (Postgres, Kafka, Grafana, Loki).
* [ ] Postgres seed via `deploy/postgres/init.sql` (4 users min).
* [ ] Redis config: leave default for dev; add `--requirepass` support behind env (off by default).
* [ ] Mosquitto dev config: `allow_anonymous true`; keep a commented secure variant.

**Verification**

```bash
make up && make health
# expect all services HEALTHY within 90s
```

---

##  Step 2 — Secrets & Env Management

* [ ] Create `deploy/.env.example` with **non‑secrets defaults**.
* [ ] Document `.env` usage in this file; never commit real secrets.
* [ ] Wire Compose services to read from `.env`.
* [ ] Add `Makefile` targets to generate `.env` from example.

**Makefile snippets**

```make
copy-env:
	@[ -f deploy/.env ] || cp deploy/.env.example deploy/.env
print-env:
	@sed 's/.*/export &/' deploy/.env
```

---

##  Step 3 — Local TLS & NGINX Gateway

* [ ] Generate dev certs with **mkcert** or **openssl** → place in `deploy/nginx/certs/`.
* [ ] Author `deploy/nginx/nginx.conf`:

  * [ ] TLS termination (443), HTTP→HTTPS redirect.
  * [ ] Routes:

    * `/` → static front (if served via NGINX) or proxy to cockpit-backend.
    * `/api/` → cockpit-backend.
    * `/ws/` → WebSocket reverse proxy (upgrade headers).
  * [ ] Rate limit basics, gzip, access logs.

**Verification**

```bash
curl -k https://localhost/healthz
# "ok" via NGINX upstream check
```

---

##  Step 4 — Messaging Contracts (authoritative)

* [ ] **Kafka topics:** `attempt-logs`, `entrance-logs`.
* [ ] **MQTT topics:** `iot/entrance/badge`, `iot/entrance/decision`.
* [ ] Add `deploy/kafka/topics-init.sh` and `deploy/kafka/topics.env`:

  * `REPLICATION_FACTOR=1`, partitions configurable.
  * Idempotent creation (`kafka-topics --if-not-exists` pattern or safe wrapper).
* [ ] JSON Schemas (draft‑07+) in `/docs/messaging/`:

  * `attempt-logs.schema.json`
  * `entrance-logs.schema.json`
  * `mqtt.json` (decision & badge payloads)
* [ ] Add **schema versioning** convention: `major.minor.patch` in `$schema` or `x-version` field.

**Hand‑off Guarantees**

* Ouadra and Hamza code **must not import** this repo to compile; they only need topics + schemas.
* Provide sample payloads + `curl`/CLI examples in each schema file.

---

##  Step 5 — Observability (Prometheus, Grafana, Loki optional)

* [ ] Add Prometheus with scrape targets: NGINX, Kafka JMX (optional), Spring Actuator, Node prom-client.
* [ ] Provision Grafana dashboards (basic): Kafka consumer lag, JVM metrics, NGINX, MQTT rate.
* [ ] (Optional) Add Loki + promtail for logs.

**Verification**

```bash
open http://localhost:3000  # Grafana
# Dashboards load without manual import
```

---

##  Step 6 — Testing Harness (Integration‑first)

* [ ] Java services use **Testcontainers** (Postgres, Redis, Kafka).
* [ ] Node services use ephemeral containers or local mocks.
* [ ] Create `/docs/api/openapi.yaml` stub + spectral lint (optional).
* [ ] Add **contract tests** validating JSON payloads vs schemas.

**Acceptance**

* `mvn test` in Spring modules spins containers and passes.
* Bridge service has Jest tests + schema validation.

---

##  Step 7 — CI/CD (GitHub Actions)

* [ ] Workflow: `ci.yml` (matrix for Java/Node).
* [ ] Jobs: checkout → setup JDK 21 & Node → build → unit tests → **integration tests with Testcontainers** → build Docker images.
* [ ] Optional: push images to GHCR on `main` merges with `:git-sha` + `:latest` tags.
* [ ] Cache Maven & npm to speed builds.

**Quality Gates**

* Require green CI, required reviewers (Ayoub) for protected paths.

---

##  Step 8 — Developer Ergonomics

* [ ] Root **Makefile** providing one‑liners:

```make
.PHONY: up down logs rebuild health seed certs topics
up: copy-env
	docker compose -f deploy/docker-compose.yml up -d

down:
	docker compose -f deploy/docker-compose.yml down -v

logs:
	docker compose -f deploy/docker-compose.yml logs -f --tail=200

health:
	docker compose -f deploy/docker-compose.yml ps

rebuild:
	docker compose -f deploy/docker-compose.yml build --no-cache

seed:
	docker exec -it postgres psql -U postgres -f /docker-entrypoint-initdb.d/init.sql

certs:
	# generate dev certs here (mkcert or openssl)

topics:
	docker compose -f deploy/docker-compose.yml exec kafka /bin/bash /kafka/topics-init.sh
```

* [ ] Pre‑commit hooks: `lint`, `format`, `secret-scan` (e.g., detect‑secrets or gitleaks).

---

##  Step 9 — Review Process & Branching

* **Branching:** `feature/*`, `fix/*`, `infra/*`.
* **Reviews:** Ayoub approves changes in `/deploy`, `/docs`, workflows.
* **Checklist for PRs:**

  * [ ] Docs updated (schemas/openapi)
  * [ ] Tests added/green
  * [ ] Topics unaffected or bump version noted
  * [ ] Security review (secrets/TLS)

---

##  Step 10 — Release & Versioning

* **SemVer for schemas**; tag repo releases `vX.Y.Z`.
* **Changelog:** group by Infra, Contracts, Services.
* **Images:** `org/badge-<service>:<git-sha>`.

---

##  Interfaces (for Isolation)

### For Ouadra

* **Kafka:** `attempt-logs` (consume), `entrance-logs` (produce on authorize).
* **MQTT:** publish `iot/entrance/decision`.
* **DB/Cache:** Redis first, Postgres fallback.
* **Config:** read endpoints from env; no hardcoded hostnames.

### For Hamza

* **MQTT → Kafka bridge:** sub `iot/entrance/badge` → pub `attempt-logs`.
* **Mocks:** `badge-sensor-mock`, `door-lock-mock`.
* **Config:** broker URLs via env; simple retry/backoff.

---

##  Commands You’ll Use Often

```bash
# Start/stop
make up
make down

# Check health & logs
make health
make logs

# Kafka topics provision
make topics

# Test MQTT quickly
docker exec mosquitto mosquitto_pub -h mosquitto -t iot/entrance/badge -m '{"badge_id":"A1","timestamp":"2025-01-01T00:00:00Z"}'
```

---

##  Troubleshooting Appendix

* **Kafka topic not found:** re‑run `make topics`; ensure broker is healthy; verify network alias `kafka` in Compose.
* **WebSocket 400 via NGINX:** check `Upgrade` and `Connection` headers; proxy_read_timeout.
* **TLS errors in browser:** trust mkcert CA or use `-k` with curl.
* **Testcontainers slow:** enable Docker resource limits; cache Maven repo in CI.

---

##  Acceptance Criteria Recap

* `make up` yields healthy infra with TLS and topic provisioning.
* Schemas + topics are **documented and versioned**.
* CI/CD pipeline builds, tests, and validates contracts on every PR.
* Grafana shows Kafka lag, MQTT rate, and NGINX metrics.
* Team can develop **without blocking** each other using these contracts.