import mqtt from "mqtt";
import chalk from "chalk";

const brokerUrl = `mqtt://${process.env.MQTT_BROKER_HOST || "mosquitto"}:${process.env.MQTT_BROKER_PORT || 1883}`;
const topic = "iot/entrance/door";

const client = mqtt.connect(brokerUrl);

client.on("connect", () => {
  console.log(chalk.blueBright(`Door-lock mock connected to ${brokerUrl}`));
  client.subscribe(topic, (err) => {
    if (err) {
      console.error("Subscription failed:", err.message);
      return;
    }
    console.log(chalk.yellow(`Listening on topic: ${topic}`));
  });
});

client.on("message", (_topic, message) => {
  let payload;
  try {
    payload = JSON.parse(message.toString());
  } catch {
    console.error("Invalid MQTT payload:", message.toString());
    return;
  }

  const { badgeId, status, eventType, fullName } = payload;

  if (eventType === "manual") {
    const verb = status === "OPEN" ? chalk.greenBright("OPEN (manual)") : chalk.redBright("CLOSE (manual)");
    console.log(`${verb} by operator`);
    return;
  }

  if (status === "GRANTED") {
    console.log(chalk.greenBright(`Door opened for ${fullName || "?"} (${badgeId})`));
  } else {
    console.log(chalk.redBright(`Access denied for ${badgeId || "?"}`));
  }
});
