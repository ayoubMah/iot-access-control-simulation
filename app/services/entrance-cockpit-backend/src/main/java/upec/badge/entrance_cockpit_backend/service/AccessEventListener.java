package upec.badge.entrance_cockpit_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import upec.badge.entrance_cockpit_backend.dto.AccessEventDTO;

@Service
public class AccessEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AccessEventListener.class);

    private final SseService sseService;

    public AccessEventListener(SseService sseService) {
        this.sseService = sseService;
    }

    @KafkaListener(topics = "${kafka.topic.access-events}", groupId = "cockpit-group")
    public void listen(AccessEventDTO event) {

        logger.info("🔥 Received access event from Kafka: {}", event);

        logger.info("📤 Forwarding event to SSE service...");
        sseService.sendEventToAll(event);
        logger.info("📤 SSE forwarding call finished.");
    }
}