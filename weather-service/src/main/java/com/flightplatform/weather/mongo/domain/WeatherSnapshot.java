package com.flightplatform.weather.mongo.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Data
@Builder
@Document(collection = "weather_snapshots")
public class WeatherSnapshot {

    @Id
    private String id;

    private String bboxId;
    private String airportIcao;
    private boolean isAirport;

    private double centerLat;
    private double centerLon;
    private double minLat;
    private double maxLat;
    private double minLon;
    private double maxLon;
    private double temperature;
    private double feelsLike;
    private double windSpeed;
    private int windDeg;
    private String weatherMain;
    private String weatherDesc;
    private int visibility;
    private int cloudiness;

    private Instant fetchedAt;
}
