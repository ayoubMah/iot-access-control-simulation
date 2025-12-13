package upec.badge.entrance_cockpit_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import upec.badge.entrance_cockpit_backend.dto.AccessEventDTO;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {

    private static final Logger logger = LoggerFactory.getLogger(SseService.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void addEmitter(SseEmitter emitter) {

        logger.info("🟢 New SSE client connected. Total: {}", emitters.size() + 1);
        emitters.add(emitter);

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            logger.info("🔴 SSE client disconnected. Total now: {}", emitters.size());
        });

        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            emitter.complete();
            logger.warn("⏳ SSE client timed out. Total now: {}", emitters.size());
        });
    }

    public void sendEventToAll(AccessEventDTO event) {

        logger.info("📡 Broadcasting SSE event to {} clients: {}", emitters.size(), event);

        for (SseEmitter emitter : emitters) {
            try {
                logger.info("➡️ Sending SSE to emitter {}", emitter);
                emitter.send(SseEmitter.event()
                        .name("access-event")
                        .data(event));
            } catch (IOException e) {
                logger.error("❌ Error sending SSE → removing emitter. Cause: {}", e.getMessage());
                emitters.remove(emitter);
            }
        }
    }
}