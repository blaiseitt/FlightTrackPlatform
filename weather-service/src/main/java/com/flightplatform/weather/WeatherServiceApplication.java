package com.flightplatform.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WeatherServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeatherServiceApplication.class, args);
    }
}

//TODO Divide bb into smaller bb, use this to fetch specific data, create topic and event for kafka and scheduler.
//Save this data to mongo, pass it somehow to tracker service and from there it will be saved.*