package com.flightplatform.weather.openweather;

import com.flightplatform.common.event.WeatherSnapshotEvent;
import com.flightplatform.weather.openweather.BoundingBoxGrid.BoundingBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@RequiredArgsConstructor
public class WeatherPollingScheduler {

    private final BoundingBoxGrid grid;
    private final OpenWeatherClient client;
    private final WeatherEventPublisher publisher;

    @Scheduled(fixedDelayString = "${weather.polling.interval-ms:60000}",
               initialDelayString = "${weather.polling.initial-delay-ms:10000}")
    void pollAllCells() {
        AtomicInteger published = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        Instant pollTime = Instant.now();

        log.info("Weather poll starting — {} cells to fetch", grid.getCells().size());

        for (BoundingBox cell : grid.getCells()) {
            Optional<OpenWeatherResponse> response = client.fetchForCell(cell);

            if (response.isEmpty()) {
                failed.incrementAndGet();
            }

            OpenWeatherResponse r = response.get();
            WeatherSnapshotEvent event = toEvent(cell, r, pollTime);
            publisher.publishWeather(event);
            published.incrementAndGet();
        }
        log.info("Weather poll complete — published={} failed={}", published.get(), failed.get());
    }

    private WeatherSnapshotEvent toEvent(BoundingBox cell, OpenWeatherResponse r, Instant fetchedAt) {
        String weatherMain = r.weather() != null && !r.weather().isEmpty()
                ? r.weather().get(0).main() : "Unknown";
        String weatherDesc = r.weather() != null && !r.weather().isEmpty()
                ? r.weather().get(0).description() : "Unknown";

        return WeatherSnapshotEvent.builder()
                .bboxId(cell.id())
                .centerLat(cell.centerLat())
                .centerLon(cell.centerLon())
                .minLat(cell.minLat())
                .maxLat(cell.maxLat())
                .minLon(cell.minLon())
                .maxLon(cell.maxLon())
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
}
