package integration;

import com.flightplatform.common.event.FlightPositionEvent;
import com.flightplatform.common.event.KafkaTopics;
import com.flightplatform.tracker.FlightTrackerServiceApplication;
import com.flightplatform.tracker.domain.Flight;
import com.flightplatform.tracker.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(classes = FlightTrackerServiceApplication.class)
@Testcontainers
class FlightKafkaIntegrationTest {

    @Container
    static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    @Container
    static MongoDBContainer mongo = new MongoDBContainer(
            DockerImageName.parse("mongo:7")
    );

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private KafkaTemplate<String, FlightPositionEvent> kafkaTemplate;

    @Autowired
    private FlightRepository flightRepository;

    @Test
    void shouldPersistFlightWhenEventConsumed() throws InterruptedException {
        FlightPositionEvent event = FlightPositionEvent.builder()
                .icao24("abc123").callsign("LOT123").build();

        kafkaTemplate.send(KafkaTopics.FLIGHT_POSITIONS, "abc123", event);
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Optional<Flight> flight = flightRepository.findById("abc123");
                    assertThat(flight).isPresent();
                    assertThat(flight.get().getCallsign()).isEqualTo("LOT123");
                });
    }
}
