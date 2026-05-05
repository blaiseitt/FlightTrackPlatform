package com.flightplatform.tracker.controller;

import com.flightplatform.tracker.controller.dto.FlightResponse;
import com.flightplatform.tracker.jpa.entity.Airline;
import com.flightplatform.tracker.mongo.domain.Flight;
import com.flightplatform.tracker.mongo.repo.FlightRepository;
import com.flightplatform.tracker.service.AirlineCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flights")
@Slf4j
@RequiredArgsConstructor
public class FlightController {

    private final FlightRepository flightRepository;
    private final AirlineCache airlineCache;

    @GetMapping("/{icao24}")
    public ResponseEntity<FlightResponse> getFlightByIcao24(@PathVariable String icao24) {
        return flightRepository.findById(icao24)
                .map(flight -> {
                    Airline airline = airlineCache
                            .findByCallsign(flight.getCallsign())
                            .orElse(null);

                    FlightResponse response = FlightResponse.builder()
                            .flight(flight)
                            .airlineName(airline != null ? airline.getName() : "Unknown")
                            .airlineCountry(airline != null ? airline.getCountry() : "Unknown")
                            .build();

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/callsign/{callsign}")
    public ResponseEntity<Flight> getFlightByCallsign(@PathVariable String callsign) {
        return flightRepository.findByCallsign(callsign.toUpperCase())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public List<Flight> getActiveFlights() {
        Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);
        List<Flight> active = flightRepository.findByLastSeenAfter(fiveMinutesAgo);
        log.debug("Active flights query returned {} results", active.size());
        return active;
    }

    @GetMapping("/airborne")
    public List<Flight> getAirborneFlights() {
        Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);
        return flightRepository.findByOnGroundFalseAndLastSeenAfter(fiveMinutesAgo);
    }

    @GetMapping("/emergency")
    public List<Flight> getEmergencyFlights() {
        List<Flight> emergency = flightRepository.findEmergencyFlights();
        if (!emergency.isEmpty()) {
            log.warn("EMERGENCY FLIGHTS ACTIVE: {}", emergency.size());
        }
        return emergency;
    }

    @GetMapping("/country/{country}")
    public List<Flight> getFlightsByCountry(@PathVariable String country) {
        return flightRepository.findByOriginCountry(country);
    }

    @GetMapping("/bbox")
    public List<Flight> getFlightsByBoundingBox(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon) {
        return flightRepository.findByBoundingBox(minLat, maxLat, minLon, maxLon);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);
        long totalTracked = flightRepository.count();
        long activeCount = flightRepository.findByLastSeenAfter(fiveMinutesAgo).size();
        long airborneCount = flightRepository.countByOnGroundFalseAndLastSeenAfter(fiveMinutesAgo);
        long emergencyCount = flightRepository.findEmergencyFlights().size();

        return Map.of(
                "totalTracked", totalTracked,
                "activeLastFiveMinutes", activeCount,
                "currentlyAirborne", airborneCount,
                "emergencySquawks", emergencyCount,
                "asOf", Instant.now().toString()
        );
    }
}
