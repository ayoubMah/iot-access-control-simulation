import json
from confluent_kafka import Producer
import paho.mqtt.client as mqtt
from dotenv import load_dotenv
import os

load_dotenv() 

KAFKA_URL = os.getenv("KAFKA_URL")
MQTT_BADGE = os.getenv("MQTT_BADGE")
MQTT_DOOR = os.getenv("MQTT_DOOR")
MQTT_BROKER_URL = os.getenv("MQTT_BROKER_URL")
MQTT_BROKER_PORT = os.getenv("MQTT_BROKER_PORT")
KAFKA_BADGE = os.getenv("KAFKA_BADGE")
KAFKA_DOOR = os.getenv("KAFKA_DOOR")

producer = Producer({'bootstrap.servers': KAFKA_URL})

def on_message(client, userdata, msg):
    try:
        payload_str = msg.payload.decode()
        payload = json.loads(payload_str)  

        if msg.topic == MQTT_BADGE:
            kafka_topic = KAFKA_BADGE
        elif msg.topic == MQTT_DOOR:
            kafka_topic = KAFKA_DOOR
        else:
            print(f"Unknown MQTT topic: {msg.topic}")
            return
        
        producer.produce(kafka_topic, json.dumps(payload))
        producer.flush()

        print(f"Forwarded from MQTT[{msg.topic}] → Kafka[{kafka_topic}]: {payload}")

    except Exception as e:
        print("Error processing MQTT message:", e)


client = mqtt.Client()
client.connect(MQTT_BROKER_URL, MQTT_BROKER_PORT)

client.subscribe(MQTT_BADGE)
client.subscribe(MQTT_DOOR)

client.on_message = on_message

print("Telemetry backend started. Listening for MQTT events...")
client.loop_forever()
