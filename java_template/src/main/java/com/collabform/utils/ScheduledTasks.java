package com.collabform.utils;

import com.collabform.service.FormResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Scheduled tasks for maintenance operations.
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final FormResponseService formResponseService;

    /**
     * Clean up expired locks every 10 seconds.
     * This helps ensure that abandoned locks do not prevent other users
     * from editing fields indefinitely.
     */
    @Scheduled(fixedRate = 10000) // 10 seconds
    public void cleanupExpiredLocks() {
        int count = formResponseService.cleanupExpiredLocks();
        if (count > 0) {
            log.info("Cleaned up {} expired locks", count);
        }
    }

    /**
     * Log system status every hour.
     * This could be extended to include more comprehensive health checks.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void logSystemStatus() {
        log.info("Collaborative Form System running normally");
        // Additional health checks could be added here
    }
}