package upec.badge.core_operational_backend.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {

    // Inject values from application.properties / env vars
    @Value("${MQTT_HOST}")
    private String mqttHost;

    @Value("${MQTT_PORT}")
    private int mqttPort;

    @Value("${mqtt.topic:iot/entrance/door}")
    private String mqttTopic;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();

        MqttConnectOptions options = new MqttConnectOptions();
        String uri = "tcp://" + mqttHost + ":" + mqttPort;
        options.setServerURIs(new String[]{uri});
        options.setCleanSession(true);

        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound(MqttPahoClientFactory mqttClientFactory) {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler("coreOperationalBackendClientId", mqttClientFactory);

        handler.setAsync(true);
        handler.setDefaultTopic(mqttTopic);
        return handler;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }
}