package com.flightplatform.tracker.service;

import com.flightplatform.common.event.FlightPositionEvent;
import com.flightplatform.tracker.mongo.domain.Flight;
import com.flightplatform.tracker.mongo.repo.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightTrackerService {

    private static final int MAX_POSITION_HISTORY = 200;

    private final FlightRepository flightRepository;

    public Flight processPosition(FlightPositionEvent event) {
        Flight flight = flightRepository.findById(event.getIcao24())
                .orElse(createNewFlight(event));

        updateCurrentPosition(flight, event);
        appendToHistory(flight, event);

        Flight saved = flightRepository.save(flight);
        log.debug("Upserted flight {}: callsign={} lat={} lon={} alt={}m onGround={}",
                saved.getIcao24(), saved.getCallsign(),
                saved.getLatitude(), saved.getLongitude(),
                saved.getBaroAltitude(), saved.getOnGround());

        return saved;
    }
    private Flight createNewFlight(FlightPositionEvent event) {
        log.info("New flight spotted: icao24={} callsign={} from {}",
                event.getIcao24(), event.getCallsign(), event.getOriginCountry());

        return Flight.builder()
                .icao24(event.getIcao24())
                .firstSeen(Instant.now())
                .positionHistory(new ArrayList<>())
                .build();
    }

    private void updateCurrentPosition(Flight flight, FlightPositionEvent event) {
        if (event.getCallsign() != null && !event.getCallsign().isBlank()) {
            flight.setCallsign(event.getCallsign());
        }
        flight.setOriginCountry(event.getOriginCountry());
        flight.setLatitude(event.getLatitude());
        flight.setLongitude(event.getLongitude());
        flight.setBaroAltitude(event.getBaroAltitude());
        flight.setGeoAltitude(event.getGeoAltitude());
        flight.setVelocity(event.getVelocity());
        flight.setTrueTrack(event.getTrueTrack());
        flight.setVerticalRate(event.getVerticalRate());
        flight.setOnGround(event.getOnGround());
        flight.setSquawk(event.getSquawk());
        flight.setLastSeen(event.getObservedAt() != null ? event.getObservedAt() : Instant.now());

        if ("7700".equals(event.getSquawk()) || "7500".equals(event.getSquawk())) {
            log.warn("EMERGENCY SQUAWK {} for flight {} ({})",
                    event.getSquawk(), event.getCallsign(), event.getIcao24());
        }
    }

    private void appendToHistory(Flight flight, FlightPositionEvent event) {
        List<Flight.PositionSnapshot> history = flight.getPositionHistory();
        if (history == null) {
            history = new ArrayList<>();
            flight.setPositionHistory(history);
        }

        history.add(Flight.PositionSnapshot.builder()
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .baroAltitude(event.getBaroAltitude())
                .velocity(event.getVelocity())
                .observedAt(event.getObservedAt() != null ? event.getObservedAt() : Instant.now())
                .build());

        if (history.size() > MAX_POSITION_HISTORY) {
            history.remove(0);
        }
    }
}
