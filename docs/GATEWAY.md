# Gateway Integration — Unified Access Point

> **Goal:** Serve both the React cockpit and Spring Boot backend through a single gateway (`http://localhost:8080`).

---

## ✅ Final URLs

| Service                                | Path        | Example                                                                            |
| -------------------------------------- | ----------- | ---------------------------------------------------------------------------------- |
| Cockpit UI                             | `/cockpit/` | [http://localhost:8080/cockpit/](http://localhost:8080/cockpit/)                   |
| Core Operational Backend (Spring Boot) | `/api/`     | [http://localhost:8080/api/people/B-0002](http://localhost:8080/api/people/B-0002) |
| Health Check                           | `/healthz`  | [http://localhost:8080/healthz](http://localhost:8080/healthz)                     |

---

```mermaid
graph TD
    Browser["Browser"] --> NGINX_Host["http://localhost:8080"]

    NGINX_Host --> NGINX{"NGINX Gateway"}

    subgraph "NGINX Routing"
        NGINX -- "/api/*" --> CoreBackend["Core Backend (8081)"]
        NGINX -- "/cockpit/*" --> CockpitFront["Cockpit Frontend (Static Assets)"]
        NGINX -- "/healthz" --> Healthz["Returns 'ok'"]
    end

    subgraph "Backend Dependencies"
        CoreBackend --> DataServices["(Postgres, Redis, Kafka)"]
    end

    subgraph "Frontend Data Flow"
        CockpitFront -- "fetches data from" --> CoreBackend
    end
```
