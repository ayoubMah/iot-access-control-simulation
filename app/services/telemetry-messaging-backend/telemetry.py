import json
import os
from confluent_kafka import Producer
import paho.mqtt.client as mqtt
from dotenv import load_dotenv

# Load env vars from Docker
load_dotenv()

# --- ENV VARIABLES ---
KAFKA_URL = os.getenv("KAFKA_URL", "kafka:9092")

MQTT_BROKER_HOST = os.getenv("MQTT_BROKER_HOST", "mqtt-broker")
MQTT_BROKER_PORT = int(os.getenv("MQTT_BROKER_PORT", "1883"))

MQTT_BADGE = os.getenv("MQTT_BADGE", "iot/entrance/badge")
MQTT_DOOR = os.getenv("MQTT_DOOR", "iot/entrance/door")

KAFKA_BADGE = os.getenv("KAFKA_BADGE", "entrance_attempts")
KAFKA_DOOR = os.getenv("KAFKA_DOOR", "entrance_logs")

# --- KAFKA PRODUCER ---
producer = Producer({'bootstrap.servers': KAFKA_URL})

# --- PROCESS MQTT MESSAGE ---
def on_message(client, userdata, msg):
    try:
        payload_str = msg.payload.decode("utf-8")
        payload = json.loads(payload_str)

        if msg.topic == MQTT_BADGE:
            kafka_topic = KAFKA_BADGE
        elif msg.topic == MQTT_DOOR:
            kafka_topic = KAFKA_DOOR
        else:
            print(f"[WARN] Unknown MQTT topic: {msg.topic}")
            return

        producer.produce(kafka_topic, json.dumps(payload).encode("utf-8"))
        producer.flush()

        print(f"[OK] MQTT[{msg.topic}] → Kafka[{kafka_topic}] :: {payload}")

    except Exception as e:
        print("[ERROR] Failed to process MQTT message:", e)

# --- MQTT CLIENT ---
client = mqtt.Client()

client.on_message = on_message

print(f"[BOOT] Connecting to MQTT broker {MQTT_BROKER_HOST}:{MQTT_BROKER_PORT}")
client.connect(MQTT_BROKER_HOST, MQTT_BROKER_PORT)

client.subscribe(MQTT_BADGE)
client.subscribe(MQTT_DOOR)

print(f"[BOOT] Subscribed to topics: {MQTT_BADGE}, {MQTT_DOOR}")
print("[RUNNING] Telemetry backend started. Waiting for MQTT events...")

# Start MQTT loop
client.loop_forever()
