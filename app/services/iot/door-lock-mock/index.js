import mqtt from "mqtt";
import chalk from "chalk";

const brokerUrl = "mqtt://mosquitto:1883"; // matches docker-compose service name
const topic = "iot/entrance/decision";

// Connect to the MQTT broker
const client = mqtt.connect(brokerUrl);

client.on("connect", () => {
  console.log(chalk.blueBright("🚪 Door-lock mock connected to MQTT broker"));
  client.subscribe(topic, (err) => {
    if (err) {
      console.error("❌ Subscription failed:", err.message);
    } else {
      console.log(chalk.yellow(`📡 Listening on topic: ${topic}`));
    }
  });
});

client.on("message", (topic, message) => {
  try {
    const payload = JSON.parse(message.toString());
    const { badge_id, status } = payload;

    if (status === "GRANTED") {
      console.log(chalk.greenBright(`🔓 Door opened for badge ${badge_id}`));
    } else {
      console.log(chalk.redBright(`⛔ Access denied for badge ${badge_id}`));
    }
  } catch (err) {
    console.error("⚠️ Invalid MQTT message:", message.toString());
  }
});
