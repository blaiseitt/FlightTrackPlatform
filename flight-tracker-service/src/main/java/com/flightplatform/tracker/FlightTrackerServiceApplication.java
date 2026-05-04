package com.flightplatform.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.flightplatform.tracker.mongo.repo")
@EnableJpaRepositories(basePackages = "com.flightplatform.tracker.jpa.repo")
public class FlightTrackerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlightTrackerServiceApplication.class, args);
    }
}
