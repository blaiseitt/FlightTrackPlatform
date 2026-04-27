package com.flightplatform.ingestion.publisher;

import com.flightplatform.common.event.FlightPositionEvent;
import com.flightplatform.common.event.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FlightEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPosition(FlightPositionEvent event) {
        kafkaTemplate.send(KafkaTopics.FLIGHT_POSITIONS, event.getIcao24(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish position for icao24={}: {}",
                                event.getIcao24(), ex.getMessage());
                    } else {
                        log.debug("Published position for {} [callsign={}] → partition={} offset={}",
                                event.getIcao24(),
                                event.getCallsign(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
