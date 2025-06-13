package com.collabform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the CollabForm application.
 * This Spring Boot application enables real-time collaborative form filling
 * with features like dynamic field creation, real-time updates, and field locking.
 */
@SpringBootApplication
@EnableScheduling
public class CollabFormApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollabFormApplication.class, args);
    }
}