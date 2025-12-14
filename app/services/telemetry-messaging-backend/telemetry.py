import json
import os
from confluent_kafka import Producer
import paho.mqtt.client as mqtt
from datetime import datetime, timezone

KAFKA_URL = os.getenv("KAFKA_URL", "kafka:9093")

MQTT_BROKER_HOST = os.getenv("MQTT_BROKER_HOST", "mosquitto")
MQTT_BROKER_PORT = int(os.getenv("MQTT_BROKER_PORT", "1883"))

MQTT_BADGE = os.getenv("MQTT_BADGE", "iot/entrance/badge")
MQTT_DOOR = os.getenv("MQTT_DOOR", "iot/entrance/door")

KAFKA_BADGE = os.getenv("KAFKA_BADGE", "entrance_attempts")
KAFKA_DOOR = os.getenv("KAFKA_DOOR", "entrance_logs")

producer = Producer({'bootstrap.servers': KAFKA_URL})


def normalize_payload(topic, payload):
    # Regular badge scan → open event
    if topic == MQTT_BADGE:
        return {
            "badgeId": payload.get("badgeId"),
            "timestamp": payload.get("timestamp")
        }

    # Manual Open
    if topic == MQTT_DOOR:
        return {
            "badgeId": payload.get("badgeId"),
            "firstName": payload.get("firstName"),
            "lastName": payload.get("lastName"),
            "eventType": payload.get("eventType"),
            "timestamp": payload.get("timestamp"),
        }

    print(f"[WARN] Unknown MQTT topic {topic}")
    return None


def on_message(client, userdata, msg):
    try:
        raw = msg.payload.decode("utf-8")
        payload = json.loads(raw)

        event = normalize_payload(msg.topic, payload)
        if event is None:
            return

        topic = KAFKA_DOOR if msg.topic == MQTT_DOOR else KAFKA_BADGE

        producer.produce(topic, json.dumps(event).encode("utf-8"))
        producer.flush()

        print(f"[OK] MQTT[{msg.topic}] → Kafka[{topic}] :: {event}")

    except Exception as e:
        print("[ERROR] Failed processing MQTT message:", e)


client = mqtt.Client()
client.on_message = on_message

print(f"[BOOT] MQTT connect {MQTT_BROKER_HOST}:{MQTT_BROKER_PORT}")
client.connect(MQTT_BROKER_HOST, MQTT_BROKER_PORT)

client.subscribe(MQTT_BADGE)
client.subscribe(MQTT_DOOR)

print("[RUNNING] Telemetry backend running.")
client.loop_forever()