package upec.badge.core_operational_backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    // This will be "CORE-A" or "CORE-B" injected from Docker
    @Value("${INSTANCE_ID:UNKNOWN}")
    private String instanceId;

    @GetMapping("/status")
    public String status() {
        return "Core Backend is ALIVE! ID: " + instanceId;
    }
}