package com.flightplatform.weather.kafka;

import com.flightplatform.common.event.KafkaTopics;
import com.flightplatform.common.event.WeatherSnapshotEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class WeatherEventPublisher {

    private final KafkaTemplate<String, WeatherSnapshotEvent> kafkaTemplate;

    public void publishWeather(WeatherSnapshotEvent event) {
        kafkaTemplate.send(KafkaTopics.WEATHER_SNAPSHOTS, event.getBboxId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish weather event for cell={}: {}",
                                event.getBboxId(), ex.getMessage());
                    }
                });
    }
}
