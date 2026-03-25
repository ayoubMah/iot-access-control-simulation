package upec.badge.entrance_cockpit_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upec.badge.entrance_cockpit_backend.service.ManualControlService;

@RestController
@RequestMapping("/api/manual")
public class ManualController {

    private final ManualControlService manualControlService;

    public ManualController(ManualControlService manualControlService) {
        this.manualControlService = manualControlService;
    }

    @PostMapping("/open")
    public ResponseEntity<Void> openDoor() {
        manualControlService.sendOpenCommand();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/close")
    public ResponseEntity<Void> closeDoor() {
        manualControlService.sendCloseCommand();
        return ResponseEntity.ok().build();
    }

}