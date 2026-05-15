package com.flightplatform.weather.service;

import com.flightplatform.weather.jpa.entity.Airport;
import com.flightplatform.weather.jpa.repo.AirportRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AirportRegistry {

    private final AirportRepository repository;
    private List<Airport> airports;

    @PostConstruct
    public void load() {
        airports = repository.findAll();
        log.info("Loaded {} airports for weather polling", airports.size());
    }

    public List<Airport> getAirports() {
        return airports;
    }
}
