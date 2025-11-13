package upec.badge.entrance_cockpit_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import upec.badge.entrance_cockpit_backend.service.SseService;

@RestController
@RequestMapping("/api/events")
public class EventStreamController {

    private final SseService sseService;

    public EventStreamController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping
    public SseEmitter streamEvents() {
        // Create an emitter with a long timeout (e.g., 30 minutes).
        SseEmitter emitter = new SseEmitter(1800000L);

        // Register the new emitter so it can receive events.
        sseService.addEmitter(emitter);

        // Send a confirmation event to the client upon connection.
        try {
            emitter.send(SseEmitter.event().name("connection-ready").data("SSE connection established"));
        } catch (Exception e) {
            // This can happen if the client disconnects immediately after connecting.
            emitter.completeWithError(e);
        }

        return emitter;
    }
}