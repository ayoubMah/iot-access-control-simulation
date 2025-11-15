import time
import json
import random
import paho.mqtt.client as mqtt
from faker import Faker

BROKER = "mqtt-broker"   # service name we'll define in docker-compose
PORT = 1883
TOPIC = "iot/entrance/badge"
fake = Faker()
BADGES = ["EMP001", "EMP002", "EMP003", "VISITOR01"]

client = mqtt.Client(protocol=mqtt.MQTTv311)
client.connect(BROKER, PORT, 60)
client.loop_start()

try:
    while True:
        badgeId = random.choice(BADGES)
        name = fake.name()
        payload = json.dumps({
            "badgeId": badgeId,
            "timestamp": time.time(),
            "name": name,
        })
        print(f"🔘 Badge scanned: {badgeId}")
        client.publish(TOPIC, payload)
        time.sleep(5)
except KeyboardInterrupt:
    print("Stopped.")
finally:
    client.loop_stop()
    client.disconnect()