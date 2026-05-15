package com.flightplatform.weather.kafka;

import com.flightplatform.common.event.KafkaTopics;
import com.flightplatform.common.event.WeatherSnapshotEvent;
import com.flightplatform.weather.mongo.repo.WeatherSnapshotRepository;
import com.flightplatform.weather.mongo.domain.WeatherSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class WeatherSnapshotConsumer {

    private final WeatherSnapshotRepository repository;

    @KafkaListener(
            topics = KafkaTopics.WEATHER_SNAPSHOTS,
            groupId = "weather-persistence-group",
            concurrency = "1"
    )
    public void onWeatherEvent(WeatherSnapshotEvent event) {
        WeatherSnapshot snapshot = toDocument(event);
        repository.save(snapshot);
        log.debug("Saved weather snapshot for cell={} temp={}°C condition={}",
                event.getBboxId(), event.getTemperature(), event.getWeatherMain());
    }

    private WeatherSnapshot toDocument(WeatherSnapshotEvent e) {
        return WeatherSnapshot.builder()
                .bboxId(e.getBboxId())
                .centerLat(e.getCenterLat())
                .centerLon(e.getCenterLon())
                .minLat(e.getMinLat())
                .maxLat(e.getMaxLat())
                .minLon(e.getMinLon())
                .maxLon(e.getMaxLon())
                .temperature(e.getTemperature())
                .feelsLike(e.getFeelsLike())
                .windSpeed(e.getWindSpeed())
                .windDeg(e.getWindDeg())
                .weatherMain(e.getWeatherMain())
                .weatherDesc(e.getWeatherDesc())
                .visibility(e.getVisibility())
                .cloudiness(e.getCloudiness())
                .fetchedAt(e.getFetchedAt())
                .build();
    }

}
