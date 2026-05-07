package com.flightplatform.tracker.controller;

import com.flightplatform.tracker.config.MongoConfig;
import com.flightplatform.tracker.jpa.entity.Airline;
import com.flightplatform.tracker.jpa.repo.AirlineRepository;
import com.flightplatform.tracker.mongo.domain.Flight;
import com.flightplatform.tracker.mongo.repo.FlightRepository;
import com.flightplatform.tracker.service.AirlineCache;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 8181)
@ContextConfiguration(classes = FlightControllerTest.TestConfig.class)
public class FlightControllerTest {

    @TestConfiguration
    @SpringBootApplication(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            FlywayAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class,
            MongoAutoConfiguration.class,
            MongoDataAutoConfiguration.class
    })
    static class TestConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AirlineRepository airlineRepository;

    @MockBean
    FlightRepository flightRepository;

    @MockBean
    AirlineCache airlineCache;

    @MockBean
    MongoConfig mongoConfig;

    @BeforeEach
    void setUp() {
        Flight mockFlight = Flight.builder()
                .icao24("48af05")
                .callsign("LOT6YQ")
                .build();

        Airline mockAirline = Airline.builder()
                .country("Poland")
                .name("LOT Polish Airlines")
                .build();

        when(flightRepository.findById("48af05"))
                .thenReturn(Optional.of(mockFlight));
        when(airlineCache.findByCallsign("LOT6YQ"))
                .thenReturn(Optional.of(mockAirline));
    }

    @Test
    void shouldReturnFlightForIcao24() throws Exception {
        mockMvc.perform(get("/api/flights/48af05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flight.icao24").value("48af05"))
                .andExpect(jsonPath("$.flight.callsign").value("LOT6YQ"))
                .andExpect(jsonPath("$.airlineName").value("LOT Polish Airlines"))
                .andExpect(jsonPath("$.airlineCountry").value("Poland"));
    }

    @Test
    void shouldReturnUnknownAirline() throws Exception {
        when(airlineCache.findByCallsign("LOT6YQ"))
                .thenReturn(Optional.empty());
        mockMvc.perform(get("/api/flights/48af05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flight.icao24").value("48af05"))
                .andExpect(jsonPath("$.flight.callsign").value("LOT6YQ"))
                .andExpect(jsonPath("$.airlineName").value("Unknown"))
                .andExpect(jsonPath("$.airlineCountry").value("Unknown"));
    }

    @Test
    void shouldReturnNotFoundIcao24() throws Exception {
        mockMvc.perform(get("/api/flights/xzy64"))
                .andExpect(status().isNotFound());
    }
}
