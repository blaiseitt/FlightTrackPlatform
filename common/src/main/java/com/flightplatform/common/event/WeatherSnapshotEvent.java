package com.flightplatform.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherSnapshotEvent {

    private String bboxId;
    private String airportIcao;   // null for grid cells, ICAO code for airports
    private boolean isAirport;

    //it is guaranteed by OpenweatherAPI that the values won't be null so can use primitives.
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

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant fetchedAt;
}
