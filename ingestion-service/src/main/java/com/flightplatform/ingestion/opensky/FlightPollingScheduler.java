package com.flightplatform.ingestion.opensky;

import com.flightplatform.common.event.FlightPositionEvent;
import com.flightplatform.ingestion.publisher.FlightEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class FlightPollingScheduler {

    private final OpenSkyClient openSkyClient;
    private final FlightEventPublisher publisher;

    private final AtomicLong totalPollsSuccess = new AtomicLong(0);
    private final AtomicLong totalPollsFailure = new AtomicLong(0);
    private final AtomicLong totalEventsPublished = new AtomicLong(0);

    @Scheduled(fixedDelayString = "${scheduling.position-poll-ms:30000}")
    public void pollFlightPositions() {
        Instant start = Instant.now();
        log.info("=== Poll #{} starting ===", totalPollsSuccess.get() + totalPollsFailure.get() + 1);

        openSkyClient.fetchStates().ifPresentOrElse(
                response -> {
                    List<FlightPositionEvent> events = mapToEvents(response);

                    if (events.isEmpty()) {
                        log.warn("OpenSky returned a response but states list was empty or all nulls");
                        return;
                    }

                    events.forEach(publisher::publishPosition);

                    long elapsed = Duration.between(start, Instant.now()).toMillis();
                    totalPollsSuccess.incrementAndGet();
                    totalEventsPublished.addAndGet(events.size());

                    log.info("✓ Poll complete: {} positions published in {}ms " +
                                    "[total: {} polls, {} events]",
                            events.size(), elapsed,
                            totalPollsSuccess.get(), totalEventsPublished.get());

                    // Log a sample of what we're seeing - helpful during first run
                    events.stream()
                            .filter(e -> e.getCallsign() != null && !e.getCallsign().isBlank())
                            .limit(5)
                            .forEach(e -> log.debug("  Sample flight: {} ({}) at lat={} lon={} alt={}m",
                                    e.getCallsign(), e.getIcao24(),
                                    e.getLatitude(), e.getLongitude(), e.getBaroAltitude()));
                },
                () -> {
                    totalPollsFailure.incrementAndGet();
                    log.warn("✗ Poll failed - OpenSky returned empty (failure #{}/{})",
                            totalPollsFailure.get(),
                            totalPollsSuccess.get() + totalPollsFailure.get());
                }
        );
    }

    private List<FlightPositionEvent> mapToEvents(OpenSkyResponse response) {
        if (response.getStates() == null) {
            return Collections.emptyList();
        }

        return response.getStates().stream()
                // Filter: must have lat and lon - otherwise no position fix
                .filter(state -> state.size() >= 7
                        && state.get(5) != null
                        && state.get(6) != null)
                .map(state -> FlightPositionEvent.builder()
                        .icao24(getString(state, 0))
                        .callsign(trimCallsign(getString(state, 1)))
                        .originCountry(getString(state, 2))
                        .longitude(getDouble(state, 5))
                        .latitude(getDouble(state, 6))
                        .baroAltitude(getDouble(state, 7))
                        .onGround(getBoolean(state, 8))
                        .velocity(getDouble(state, 9))
                        .trueTrack(getDouble(state, 10))
                        .verticalRate(getDouble(state, 11))
                        .geoAltitude(getDouble(state, 13))
                        .squawk(getString(state, 14))
                        .observedAt(state.get(3) != null
                                ? Instant.ofEpochSecond(((Number) state.get(3)).longValue())
                                : null)
                        .publishedAt(Instant.now())
                        .build())
                .toList();
    }

    private String getString(List<Object> state, int index) {
        if (index >= state.size() || state.get(index) == null) return null;
        return state.get(index).toString();
    }

    private Double getDouble(List<Object> state, int index) {
        if (index >= state.size() || state.get(index) == null) return null;
        return ((Number) state.get(index)).doubleValue();
    }

    private Boolean getBoolean(List<Object> state, int index) {
        if (index >= state.size() || state.get(index) == null) return null;
        return (Boolean) state.get(index);
    }

    private String trimCallsign(String callsign) {
        return callsign != null ? callsign.trim() : null;
    }
}
