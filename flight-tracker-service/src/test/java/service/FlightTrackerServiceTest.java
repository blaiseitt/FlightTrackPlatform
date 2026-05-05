package service;

import com.flightplatform.common.event.FlightPositionEvent;
import com.flightplatform.tracker.mongo.domain.Flight;
import com.flightplatform.tracker.mongo.repo.FlightRepository;
import com.flightplatform.tracker.service.FlightTrackerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightTrackerServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightTrackerService service;

    @Test
    void shouldCapPositionHistoryAt200() {
        Flight existing = Flight.builder()
                .icao24("abc123").build();
        IntStream.range(0, 200)
                .forEach(i -> existing.getPositionHistory().add(Flight.PositionSnapshot.builder().build()));

        when(flightRepository.findById("abc123")).thenReturn(Optional.of(existing));
        when(flightRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FlightPositionEvent event = FlightPositionEvent.builder()
                .icao24("abc123").build();
        service.processPosition(event);

        ArgumentCaptor<Flight> captor = ArgumentCaptor.forClass(Flight.class);
        verify(flightRepository).save(captor.capture());
        assertThat(captor.getValue().getPositionHistory()).hasSize(200);
    }
}
