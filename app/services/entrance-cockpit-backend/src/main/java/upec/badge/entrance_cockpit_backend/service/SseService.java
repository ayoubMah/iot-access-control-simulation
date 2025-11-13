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

    // Use a thread-safe list to manage emitters, preventing ConcurrentModificationException.
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void addEmitter(SseEmitter emitter) {
        this.emitters.add(emitter);
        logger.info("New SSE client connected. Total clients: {}", emitters.size());

        // Remove emitter from list on completion or timeout to prevent memory leaks.
        emitter.onCompletion(() -> {
            this.emitters.remove(emitter);
            logger.info("SSE client disconnected. Total clients: {}", emitters.size());
        });
        emitter.onTimeout(() -> {
            emitter.complete();
            this.emitters.remove(emitter);
            logger.warn("SSE client timed out. Total clients: {}", emitters.size());
        });
    }

    // Method to broadcast an event to all connected clients.
    public void sendEventToAll(AccessEventDTO event) {
        logger.info("Broadcasting event to {} clients: {}", emitters.size(), event);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("access-event").data(event));
            } catch (IOException e) {
                logger.error("Error sending event to client. Removing emitter.", e);
                // If sending fails, assume client is disconnected and remove them.
                this.emitters.remove(emitter);
            }
        }
    }
}