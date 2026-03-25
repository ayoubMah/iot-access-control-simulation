package com.upec.watchdogapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WatchdogService {

    private static final Logger log = LoggerFactory.getLogger(WatchdogService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${watchdog.target-url}")
    private String targetUrl;

    @Value("${watchdog.backup-container}")
    private String backupContainer;

    private boolean backupStarted = false;

    @Scheduled(fixedRateString = "${watchdog.check-interval-ms}")
    public void monitor() {
        if (backupStarted) {
            log.info("System running on backup instance. Monitoring paused.");
            return;
        }

        try {
            restTemplate.getForObject(targetUrl, String.class);
            log.info("Primary instance is ALIVE at {}", targetUrl);
        } catch (Exception e) {
            log.warn("Primary instance DOWN ({}). Starting backup container '{}'...", e.getMessage(), backupContainer);
            startBackup();
        }
    }

    private void startBackup() {
        try {
            Process process = new ProcessBuilder("docker", "start", backupContainer).start();
            process.waitFor();
            log.info("Backup container '{}' started successfully.", backupContainer);
            backupStarted = true;
        } catch (Exception e) {
            log.error("Failed to start backup container '{}'", backupContainer, e);
        }
    }
}