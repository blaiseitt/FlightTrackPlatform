package com.flightplatform.tracker.mongo.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Document(collection = "flights")
public class Flight {

    @Id
    private String icao24;

    @Indexed
    private String callsign;

    private String originCountry;

    // Current position
    private Double latitude;
    private Double longitude;
    private Double baroAltitude;
    private Double geoAltitude;
    private Double velocity;
    private Double trueTrack;
    private Double verticalRate;
    private Boolean onGround;
    private String squawk;

    private Instant lastSeen;
    private Instant firstSeen;

    @Builder.Default
    private List<PositionSnapshot> positionHistory = new ArrayList<>();

    @Data
    @Builder
    public static class PositionSnapshot {
        private Double latitude;
        private Double longitude;
        private Double baroAltitude;
        private Double velocity;
        private Instant observedAt;
    }
}