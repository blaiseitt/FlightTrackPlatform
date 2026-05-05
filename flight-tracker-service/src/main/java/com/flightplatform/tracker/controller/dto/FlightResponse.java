package com.flightplatform.tracker.controller.dto;

import com.flightplatform.tracker.mongo.domain.Flight;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FlightResponse {
    private Flight flight;
    private String airlineName;
    private String airlineCountry;
}