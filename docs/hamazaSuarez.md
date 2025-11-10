# Hamza Scope — Messaging Bridge & IoT Simulation

> **Scope:** Simpler, self‑contained tasks focused on Node.js services — MQTT↔Kafka bridge and IoT mock devices (badge sensor + door lock).

---

##  Objectives

* Implement the **MQTT to Kafka bridge** service.
* Create **IoT mock clients** that simulate badge scans and door lock behavior.
* Ensure data flows correctly between MQTT, Kafka, and the backends.

---

##  Ownership & Interfaces

* **Owner:** Hamza
* **Interfaces:**

  * From Ayoub → you get the running infrastructure (Kafka + Mosquitto).
  * From Ouadra → your Kafka events (`attempt-logs`) are consumed by her core backend.
* **You provide:** Node.js services producing realistic badge data and bridging messages.

---

##  Tools & Stack

* Node.js LTS (v20+)
* Libraries: `mqtt`, `kafkajs`, `dotenv`, `winston` (optional for logging)
* Environment from Ayoub’s `.env` (use hostnames `kafka` and `mosquitto`)

---

##  Step 1 — MQTT ↔ Kafka Bridge

### Purpose

Bridge messages from the IoT layer (MQTT) to Kafka topics so Ouadra’s backend can process them.

### Folder

`/services/telemetry-messaging-backend`

### Tasks

* [ ] Create `index.js`:

  ```js
  import mqtt from 'mqtt';
  import { Kafka } from 'kafkajs';
  import dotenv from 'dotenv';
  dotenv.config();

  const mqttClient = mqtt.connect('mqtt://mosquitto:1883');
  const kafka = new Kafka({ brokers: ['kafka:9092'] });
  const producer = kafka.producer();

  await producer.connect();

  mqttClient.on('connect', () => {
    console.log(' MQTT connected');
    mqttClient.subscribe('iot/entrance/badge');
  });

  mqttClient.on('message', async (topic, message) => {
    try {
      const payload = JSON.parse(message.toString());
      console.log(' Received badge scan:', payload);
      await producer.send({
        topic: 'attempt-logs',
        messages: [{ key: payload.badge_id, value: JSON.stringify(payload) }],
      });
      console.log(' Sent to Kafka');
    } catch (err) {
      console.error(' Error processing message', err);
    }
  });
  ```
* [ ] Add Dockerfile:

  ```Dockerfile
  FROM node:20
  WORKDIR /app
  COPY package*.json ./
  RUN npm install --production
  COPY . .
  CMD ["node", "index.js"]
  ```

**Done when:** Sending an MQTT badge scan publishes a message in Kafka topic `attempt-logs`.

---

##  Step 2 — Badge Sensor Mock

### Purpose

Simulate employee badge scans and publish them to MQTT.

### Folder

`/services/iot/badge-sensor-mock`

### Tasks

* [ ] Create `index.js`:

  ```js
  import mqtt from 'mqtt';
  const client = mqtt.connect('mqtt://mosquitto:1883');

  client.on('connect', () => {
    console.log('Badge sensor connected');

    // Periodic fake scans every 10s
    setInterval(() => {
      const badgeId = `B-${Math.floor(Math.random() * 1000).toString().padStart(4, '0')}`;
      const payload = JSON.stringify({ badge_id: badgeId, timestamp: new Date().toISOString() });
      client.publish('iot/entrance/badge', payload);
      console.log(' Published scan:', payload);
    }, 10000);
  });
  ```
* [ ] Add Dockerfile similar to bridge.
* [ ] Add a flag (env var) to switch between auto mode and manual CLI mode.

**Done when:** Console shows periodic badge messages and `kafka-console-consumer` confirms they arrive.

---

##  Step 3 — Door Lock Mock

### Purpose

Simulate a door reacting to authorization decisions.

### Folder

`/services/iot/door-lock-mock`

### Tasks

* [ ] Create `index.js`:

  ```js
  import mqtt from 'mqtt';
  const client = mqtt.connect('mqtt://mosquitto:1883');

  client.on('connect', () => {
    console.log('Door lock mock ready');
    client.subscribe('iot/entrance/decision');
  });

  client.on('message', (topic, message) => {
    const decision = JSON.parse(message.toString());
    if (decision.status === 'GRANTED') {
      console.log(` Door opened for ${decision.badge_id}`);
    } else {
      console.log(` Access denied for ${decision.badge_id}`);
    }
  });
  ```

**Done when:** Publishing decision MQTT messages shows the correct lock behavior.

---

##  Step 4 — Compose Integration

* [ ] Add all three services to `docker-compose.yml` with restart policies.
* [ ] Ensure they depend on `kafka` and `mosquitto`.
* [ ] Test end‑to‑end:

  ```bash
  docker compose up -d
  docker compose logs -f telemetry-messaging-backend badge-sensor-mock door-lock-mock
  ```

**Flow to Verify**

1. Badge sensor → publishes MQTT message.
2. Bridge → forwards to Kafka.
3. Ouadra’s backend → consumes, validates, publishes decision.
4. Door lock → logs GRANTED or DENIED.

---

##  Step 5 — Testing & Debug

* [ ] Add console logging and error catching.
* [ ] Test each container individually.
* [ ] Use `mosquitto_sub -t iot/#` to inspect traffic.

**Done when:** End‑to‑end message loop works automatically within 2–3 seconds.

---

##  Acceptance Criteria

* Badge sensor publishes scans on schedule.
* Bridge forwards scans to Kafka without data loss.
* Door lock receives and displays authorization decisions.
* Logs show stable flow from MQTT → Kafka → Backend → MQTT.
