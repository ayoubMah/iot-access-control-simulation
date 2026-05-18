# Future Improvements & Nice-to-Haves

Curated backlog of ideas beyond what's already tracked in [ROADMAP.md](ROADMAP.md). Each item is scoped small enough to land in a single PR. Pick freely — none of these are urgent.

---

## Reliability

- **Outbox pattern for Kafka audit events.** Today `core-operational-backend` writes to Postgres and Kafka in the same handler. If Kafka is down, the badge decision still happens but the audit event is lost. Persist the event to an `audit_outbox` table inside the same JPA transaction and ship it to Kafka from a polling worker.
- **MQTT QoS + retained messages.** `DefaultMqttPahoClientFactory` uses QoS 0 by default. For door commands, bump to QoS 1 with `setRetained(true)` so a door-lock that reconnects sees the latest state.
- **Dead-letter topic for the SSE consumer.** `AccessEventListener` swallows malformed Kafka messages today. Configure `DefaultErrorHandler` + a `*.DLT` topic so bad payloads don't poison the consumer.
- **Idempotent badge scans.** If the same badge is scanned twice in <1s, two MQTT/Kafka events fire. Add a short Redis-backed dedup window keyed by `badgeId` + 1s bucket.
- **Graceful shutdown.** Spring Boot defaults to immediate shutdown. Set `server.shutdown=graceful` and a 30s timeout so in-flight scans complete on container restart.

## Security

- **Replace `.env` with Docker secrets** (already in roadmap, but worth detailing): mount `/run/secrets/postgres_password` and read via `SPRING_DATASOURCE_PASSWORD_FILE` or a Spring `@Value("${secret.path}")` indirection.
- **Drop host port mappings** for Postgres/Redis/Kafka/Zookeeper in compose. They're only needed for ad-hoc CLI access; create a separate `docker-compose.debug.yml` overlay that exposes them.
- **Pin all image digests.** Replace `nginx:alpine` etc. with `nginx@sha256:...` to make builds reproducible and resist tag hijacking.
- **MQTT auth.** Mosquitto runs with `allow_anonymous true`. Add username/password (or mTLS) and have the publishers authenticate. Required if this ever leaves localhost.
- **NGINX rate limiting** on `/api/people/` and `/api/manual/`. `limit_req_zone` with a low burst stops a runaway badge sensor from DOSing the core backend.
- **Trivy / grype scan in CI.** Catch CVEs in the base images before they ship.

## Observability

- **Structured JSON logs** (Logback `LogstashEncoder`) — already loosely planned for Loki, but worth doing first so logs are queryable even without a stack.
- **Correlation IDs.** Generate a UUID on every badge scan, propagate it via MQTT/Kafka headers and HTTP `X-Request-Id`, log it everywhere. Single most useful debugging tool.
- **Custom Micrometer metrics:**
  - `access.decisions.total{status="granted|denied"}` counter
  - `cache.lookup{result="hit|miss"}` counter
  - `mqtt.publish.latency` timer
  - `sse.connections.active` gauge
- **`/actuator/info`** with build info + git commit SHA, so the cockpit UI can show "build abc1234 deployed Mon".
- **Healthcheck depth.** Today `/status` returns a string. Promote to `/actuator/health` with custom `HealthIndicator`s for Postgres, Redis, Kafka, MQTT — so a service is only "healthy" when its dependencies actually answer.

## Performance

- **Cache TTL.** Cache-loader writes Redis keys with no TTL. If a `Person` is soft-deleted in Postgres, the cache lies forever. Add `SET ... EX <seconds>` and re-populate periodically.
- **Connection pool tuning.** Default Hikari pool is 10. For this workload, 5 is enough; lowering it frees Postgres connections.
- **Async MQTT publishing.** `MqttPahoMessageHandler.setAsync(true)` is already set — good — but the controller still blocks on the channel send because `DirectChannel` runs handler in caller thread. Switch to `ExecutorChannel` to actually decouple.
- **HTTP/2 between cockpit-backend and core-backend.** Spring `RestClient` + HTTP/2 saves the connection setup overhead on manual override calls.

## Developer Experience

- **Devcontainer / `.devcontainer/devcontainer.json`** so VS Code "Reopen in Container" gives a working JDK 21 + Node 20 + Docker Compose environment.
- **Pre-commit hooks** (`pre-commit` framework): `mvn spotless:check`, `eslint`, trailing-whitespace, YAML lint. Stops the easy mistakes from reaching CI.
- **Make a `Justfile` / `Makefile`** so `just up`, `just rebuild core`, `just logs`, `just seed` replace the long `docker compose -f deploy/compose/...` commands documented in the README.
- **Hot-reload the cockpit UI** against the running stack: a `docker-compose.dev.yml` overlay that mounts `app/web/entrance-cockpit-front/src` and runs `vite dev` instead of nginx-serving the build.
- **Seed data CLI.** A small script (Node or Python) that POSTs random badge scans through the cockpit at a configurable rate — useful for demos and load smoke tests.
- **`.editorconfig`** so contributors don't mix tabs and spaces across services.

## Testing

- **Testcontainers integration tests** (already in roadmap): one per service, spinning up the actual Postgres/Redis/Kafka/Mosquitto. The end-to-end MQTT flow is exactly the kind of thing unit tests can't catch.
- **Contract tests for the MQTT and Kafka payloads.** A JSON schema in `docs/contracts/` and a test in each producer + consumer that validates against it. Would have caught the `badge_id` vs `badgeId` mismatch instantly.
- **Playwright smoke test** for the cockpit: scan → toast appears → history grows.
- **`@Sql`-based test data.** Drop fixtures into `src/test/resources/sql/` and load per-test rather than relying on the compose seed.

## Frontend

- **Skeleton states** while SSE is reconnecting — today the UI just goes silent.
- **Toasts for denials** distinct from grants (different color, sound). Currently both show similarly.
- **Persistent event history** in `localStorage` so a refresh doesn't lose context.
- **`useEventSource` hook** with auto-reconnect + exponential backoff. The browser will reconnect by default but the gap is visible to users.
- **Dark mode toggle** — already have `next-themes` in deps, just unused.
- **Bundle audit.** `package.json` has `encore`, `encore.dev`, `mobx`, `mobx-lite`, `mobx-react-lite`, `toaster`, `serve` — none appear in the actual source. Remove unused deps to cut install time.

## Architecture / Features

- **Multi-entrance support** (already roadmap'd, worth fleshing out): topic per door (`iot/entrance/{doorId}/door`), `door_policy` table with `(role, door_id, allowed)` rows. Core backend reads policy on grant decision.
- **Visitor badges** — `expires_at` column on `registered_people`, cache TTL set to match expiry, denial reason `EXPIRED` distinct from `INACTIVE`.
- **Time-window policies** — "this badge works only 08:00–18:00 weekdays". A `door_schedule` table joined on decision.
- **Anomaly detector** — a separate consumer of `access-events` that flags impossible-travel (same badge scanned at two doors <60s apart) and denial bursts (same badge >5 denials in 1min).
- **Admin UI** for CRUD on `registered_people` — currently rows can only be inserted via SQL.
- **WebAuthn / FIDO2 step-up** for sensitive doors. The cockpit prompts an operator for a hardware-key tap before opening a high-security door manually.

## Operations

- **Backup script for Postgres** (`pg_dump | aws s3 cp` style) running on a cron container.
- **Compose profile for "data-only"** so you can `docker compose --profile data up` and develop a backend locally against the containerized DB/Redis/Kafka without rebuilding the JVM services.
- **Resource limits** (`mem_limit`, `cpus`) on every compose service. Today a runaway Kafka can starve the host.
- **`docker-compose.prod.yml`** overlay: no host port exposure, JVM heap caps, smaller log driver options.

## Documentation

- **ADRs (Architecture Decision Records)** under `docs/adr/` for non-obvious choices: why MQTT *and* Kafka, why Redis-first lookup, why SSE not WebSockets.
- **Sequence diagram** for the manual override flow — the current Mermaid graph shows components but not the request order, which trips new readers.
- **Failure-mode table** — for each "what if X is down", what does the system do? Useful for the watchdog/HA conversation when it comes back.
