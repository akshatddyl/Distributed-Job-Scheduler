package com.distributedjobscheduler.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // <-- The missing import!

// This tells Spring Boot to look outside the 'core' folder and scan EVERYTHING!
@SpringBootApplication(scanBasePackages = "com.distributedjobscheduler")
@EnableScheduling // <-- Wakes up your DelayQueuePoller!
public class RunnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunnerApplication.class, args);
    }
}