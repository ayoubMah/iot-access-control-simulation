# ProjectKafka – Current State & Next Steps

This document summarizes **everything we accomplished**, the **current validated system state**, and the **next steps** required to fully complete the IoT Access Control Simulation.

---

# ✅ 1. What Has Been Accomplished

## **1.1 Infrastructure (Docker + Nginx + Services)**

* All containers build successfully.
* NGINX routing is now fully working:

  * `/cockpit/` → Cockpit frontend
  * `/assets/*` → proxied correctly to cockpit-front
  * `/api/people/*` → Core backend
  * `/api/events` → Cockpit backend (SSE)
  * `/api/manual/*` → Cockpit backend (manual control)
* Kafka, Redis, PostgreSQL, Mosquitto all start properly.

---

## **1.2 Frontend (cockpit UI)**

* Vite build fixed.
* Tailwind enabled and compiled.
* Assets served correctly.
* UI renders properly with full styling.
* SSE successfully connects (`"SSE connected"`).
* Frontend proxy config for development working.

---

## **1.3 Backend Fixes**

### **Redis Compatibility Fixed**

* Added `RedisConfig` to core-operational-backend.
* Same serializer (`GenericJackson2JsonRedisSerializer`) as cache-loader.
* Eliminated Redis `StreamCorruptedException`.

### **Manual Door Control API**

* Added new controller in core-operational-backend:

  * `POST /api/core/manual/open`
  * `POST /api/core/manual/close`
* Manual open/close returns `200 OK`.

### **SSE Endpoint**

* `/api/events` routing fixed.
* Frontend now connects to SSE stream.

---

# 🔍 2. Current State (as of now)

## **2.1 Working Components**

* ✔ NGINX routing (front + backend)
* ✔ Frontend (React + Tailwind)
* ✔ Live UI rendering
* ✔ SSE connection established
* ✔ Manual open API reachable

## **2.2 Partially Working**

* ⚠ SSE events show up in browser console only when events are produced.
* ⚠ Kafka and MQTT need verification via logs.

## **2.3 Still Failing**

### **Badge Scan → `/api/people/{badgeId}` returns 500**

Possible causes:

* Redis deserialization still failing **OR**
* PostgreSQL lookup failing **OR**
* Kafka or MQTT throwing exception during event publish.

Next step: inspect logs.

---

# 🧪 3. Remaining Debug Tasks

## **3.1 Get core backend logs**

We need to read the real exception:

```
docker logs core-operational-backend | tail -n 50
```

This will tell us what exactly causes the 500.

---

## **3.2 Verify Database Records**

Check if badge exists:

```
docker exec -it badge-postgres psql -U postgres -d <your_db>
SELECT * FROM registered_person;
```

If table empty → repository returns empty → additional null-related logic runs.

---

## **3.3 Verify Kafka Producer Runs**

Run a consumer:

```
docker exec -it badge-kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic entrance_attempts
```

Trigger manual open → should produce:

```
MANUAL-OPEN
```

---

## **3.4 Verify MQTT Publisher**

Look at mock door logs:

```
docker logs badge-door-lock-mock
```

When manual open is called, it should show:

```
Door opened!
```

---

# 🚀 4. Next Steps Roadmap

## **4.1 Fix `/api/people/{badgeId}` (Highest Priority)**

* Inspect core backend logs
* Patch Redis, DB, Kafka, or MQTT issues depending on stacktrace
* Validate DB model and test lookup manually

---

## **4.2 Finish Event Flow End-to-End**

* Make sure:

  * Badge scan → core backend
  * Kafka event published
  * Cockpit-backend consumes Kafka message
  * Cockpit backend pushes SSE event
  * UI receives & displays

---

## **4.3 Prepare Documentation**

* Architecture diagram
* Service descriptions
* Event flow diagram (MQTT + Kafka + SSE)
* Developer onboarding
* Production readiness notes

---

## **4.4 Add Simulated Badge Reader**

* A Node or Python script to publish badge scans automatically.

---

## **4.5 Final Polish**

* Docker healthchecks
* Logging improvements
* Error pages and fallback
* Docker-compose.production.yml

---

# 🏁 5. Summary

You have now reached a **fully running UI**, a **working SSE pipe**, full **NGINX routing**, and a **functional manual control path**.

Next, we will:

1. Fix the last backend 500 error.
2. Complete the full event pipeline.
3. Finalize documentation.
4. Provide full project roadmap & diagrams.

---

When you're ready, run this and send me the output:

```
docker logs core-operational-backend | tail -n 50
```

I will then finish the last backend fix and complete the pipeline.
