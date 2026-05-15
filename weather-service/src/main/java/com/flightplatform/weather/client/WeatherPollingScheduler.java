package com.flightplatform.weather.client;

import com.flightplatform.common.event.WeatherSnapshotEvent;
import com.flightplatform.weather.kafka.WeatherEventPublisher;
import com.flightplatform.weather.service.AirportRegistry;
import com.flightplatform.weather.service.BoundingBoxGrid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class WeatherPollingScheduler {

    private final BoundingBoxGrid grid;
    private final OpenWeatherClient client;
    private final WeatherEventPublisher publisher;
    private final AirportRegistry airportRegistry;

    @Scheduled(fixedDelayString = "${weather.polling.grid.interval-ms:1800000}",
               initialDelayString = "${weather.polling.grid.initial-delay-ms:30000}")
    void pollAllCells() {

        log.info("Grid weather poll starting: {} cells to fetch", grid.getCells().size());

        List<PollTarget> pollTargetList = grid.getCells().stream()
                .map(cell -> new PollTarget(
                        cell.id(), null, false,
                        cell.centerLat(), cell.centerLon(),
                        cell.minLat(), cell.maxLat(),
                        cell.minLon(), cell.maxLon()))
                .toList();

        int published = pollAll(pollTargetList);

        log.info("Grid weather poll complete: published={} failed={}", published, pollTargetList.size() - published);
    }

    @Scheduled(//
            fixedDelayString   = "${weather.polling.airports.interval-ms:3600000}",
            initialDelayString = "${weather.polling.airports.initial-delay-ms:120000}")
    public void pollAirports() {

        log.info("Airport weather poll starting: {} airports", airportRegistry.getAirports().size());

        List<PollTarget> pollTargetList = airportRegistry.getAirports().stream()
                .map(airport -> new PollTarget(
                        airport.getIcaoCode(), airport.getIcaoCode(), true,
                        airport.getLatitude(), airport.getLongitude(),
                        airport.getLatitude(), airport.getLatitude(),
                        airport.getLongitude(), airport.getLongitude()))
                .toList();


        int published = pollAll(pollTargetList);

        log.info("Airport weather poll complete: published={} failed={}", published, pollTargetList.size() - published);
    }

    private WeatherSnapshotEvent toEvent(PollTarget t, OpenWeatherResponse r, Instant fetchedAt) {
        String weatherMain = r.weather() != null && !r.weather().isEmpty()
                ? r.weather().get(0).main() : "Unknown";
        String weatherDesc = r.weather() != null && !r.weather().isEmpty()
                ? r.weather().get(0).description() : "Unknown";

        return WeatherSnapshotEvent.builder()
                .bboxId(t.bboxId)
                .airportIcao(t.airportIcao)
                .isAirport(t.isAirport)
                .centerLat(t.centerLat)
                .centerLon(t.centerLon)
                .minLat(t.minLat)
                .maxLat(t.maxLat)
                .minLon(t.minLon)
                .maxLon(t.maxLon)
                .temperature(r.main().temp())
                .feelsLike(r.main().feelsLike())
                .windSpeed(r.wind().speed())
                .windDeg(r.wind().deg())
                .weatherMain(weatherMain)
                .weatherDesc(weatherDesc)
                .visibility(r.visibility())
                .cloudiness(r.clouds().all())
                .fetchedAt(fetchedAt)
                .build();
    }

    private int pollAll(List<PollTarget> targets) {
        int published = 0;
        Instant pollTime = Instant.now();

        for (PollTarget t : targets) {
            Optional<OpenWeatherResponse> response = client.fetchForCoordinates(t.centerLat, t.centerLon);

            if (response.isEmpty()) continue;

            OpenWeatherResponse r = response.get();
            WeatherSnapshotEvent event = toEvent(t, r, pollTime);
            publisher.publishWeather(event);
            published++;
        }

        return published;
    }

    private record PollTarget(
            String bboxId,
            String airportIcao,
            boolean isAirport,
            double centerLat, double centerLon,
            double minLat, double maxLat,
            double minLon, double maxLon
    ) {}
}
