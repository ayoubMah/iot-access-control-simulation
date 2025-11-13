package upec.badge.entrance_cockpit_backend.service;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ManualControlService {
    private static final Logger logger = LoggerFactory.getLogger(ManualControlService.class);

    private final MqttClient mqttClient;
    private final String mqttTopic;

    public ManualControlService(MqttClient mqttClient, @Value("${mqtt.topic.manual-control}") String mqttTopic) {
        this.mqttClient = mqttClient;
        this.mqttTopic = mqttTopic;
    }

    public void sendDoorCommand(String command) {
        try {
            if (!mqttClient.isConnected()) {
                logger.warn("MQTT client is not connected. Attempting to reconnect...");
                mqttClient.reconnect();
            }
            MqttMessage message = new MqttMessage(command.getBytes());
            message.setQos(1); // At least once delivery
            mqttClient.publish(mqttTopic, message);
            logger.info("Published manual command '{}' to MQTT topic '{}'", command, mqttTopic);
        } catch (Exception e) {
            logger.error("Failed to publish manual command to MQTT", e);
            // Optionally, re-throw as a custom exception to be handled by the controller.
            throw new RuntimeException("Failed to send command to door lock", e);
        }
    }
}