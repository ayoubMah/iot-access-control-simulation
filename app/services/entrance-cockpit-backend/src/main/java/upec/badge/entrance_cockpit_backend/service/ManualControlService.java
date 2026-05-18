package upec.badge.entrance_cockpit_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ManualControlService {

    private static final Logger logger = LoggerFactory.getLogger(ManualControlService.class);

    private final RestTemplate restTemplate;
    private final String coreBackendUrl;

    public ManualControlService(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${app.core-backend.url}") String coreBackendUrl
    ) {
        this.restTemplate = restTemplateBuilder.build();
        this.coreBackendUrl = coreBackendUrl;
    }

    /**
     * Sends a command to the core-operational-backend to manually open the door.
     */
    public void sendOpenCommand() {
        String url = coreBackendUrl + "/api/core/manual/open";
        sendCommand(url, "OPEN");
    }

    /**
     * Sends a command to the core-operational-backend to manually close the door.
     */
    public void sendCloseCommand() {
        String url = coreBackendUrl + "/api/core/manual/close";
        sendCommand(url, "CLOSE");
    }

    private void sendCommand(String url, String commandType) {
        try {
            logger.info("Sending manual {} command to core backend at: {}", commandType, url);

            // We expect an empty response with a 200 OK status.
            ResponseEntity<Void> response = restTemplate.postForEntity(url, null, Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully sent {} command to core backend.", commandType);
            } else {
                // This case is less likely with postForEntity but good for robustness.
                logger.error(
                        "Core backend responded with non-success status: {} for {} command",
                        response.getStatusCode(), commandType
                );
                throw new RuntimeException("Failed to issue manual command. Core backend returned status " + response.getStatusCode());
            }

        } catch (RestClientException e) {
            logger.error("Error communicating with core backend for manual {} command.", commandType, e);
            // Re-throw a more specific exception to be handled by a global exception handler if needed.
            throw new RuntimeException("Could not send manual command to core backend: " + e.getMessage(), e);
        }
    }
}