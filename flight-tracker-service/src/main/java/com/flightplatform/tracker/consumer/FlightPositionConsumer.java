package com.flightplatform.tracker.consumer;

import com.flightplatform.common.event.FlightPositionEvent;
import com.flightplatform.common.event.KafkaTopics;
import com.flightplatform.tracker.service.FlightTrackerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
@RequiredArgsConstructor
public class FlightPositionConsumer {

    private final FlightTrackerService trackerService;

    private final AtomicLong messagesProcessed = new AtomicLong(0);

    @KafkaListener(
            topics = KafkaTopics.FLIGHT_POSITIONS,
            groupId = "flight-tracker-group",
            concurrency = "1"
    )
    public void onFlightPosition(
            @Payload FlightPositionEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            trackerService.processPosition(event);

            long count = messagesProcessed.incrementAndGet();
            if (count % 100 == 0) {
                log.info("Flight tracker processed {} messages total", count);
            }

        } catch (Exception e) {
            log.error("Failed to process position event for icao24={} from partition={} offset={}: {}",
                    event.getIcao24(), partition, offset, e.getMessage(), e);
        }
    }
}