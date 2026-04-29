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
public class FlightPositionEvent {

    private String icao24;

    private String callsign;

    private String originCountry;

    private Double longitude;

    private Double latitude;

    private Double baroAltitude;

    private Double geoAltitude;

    private Double velocity;

    private Double trueTrack;

    private Double verticalRate;

    private Boolean onGround;

    private String squawk;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant observedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant publishedAt;
}
